//Autor Xavier Tacuri

package controlador;

import modelo.persona;
import modelo.personaDAO;
import vista.ventana;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class logica_ventana implements ActionListener, ListSelectionListener, ItemListener, KeyListener {
    private ventana delegado;
    private String nombres, email, telefono, categoria = "";
    private persona persona;
    private List<persona> contactos;
    private boolean favorito = false;
    private TableRowSorter<DefaultTableModel> sorter;

    // Worker para realizar la búsqueda en segundo plano.
    private SwingWorker<RowFilter<DefaultTableModel, Object>, Void> busquedaWorker;

    // ExecutorService para exportar contactos sin congelar la interfaz.
    private final ExecutorService exportExecutor = Executors.newFixedThreadPool(2);

    // Bloqueos para evitar condiciones de carrera al escribir archivos o modificar datos.
    private final Object exportLock = new Object();
    private final Object contactosLock = new Object();

    // Bloqueo explícito para edición/modificación de contactos.
    private final ReentrantLock bloqueoEdicion = new ReentrantLock();

    public logica_ventana(ventana delegado) {
        this.delegado = delegado;

        sorter = new TableRowSorter<>(delegado.tableModel);
        delegado.tbl_contactos.setRowSorter(sorter);

        cargarContactosRegistrados();
        registrarEventos();
        actualizarEstadisticas();
    }

    private void registrarEventos() {
        delegado.btn_add.addActionListener(this);
        delegado.btn_eliminar.addActionListener(this);
        delegado.btn_modificar.addActionListener(this);
        delegado.btn_exportar.addActionListener(this);

        delegado.cmb_categoria.addItemListener(this);
        delegado.chb_favorito.addItemListener(this);

        delegado.txt_buscar.addKeyListener(this);
        delegado.txt_nombre.addKeyListener(this);
        delegado.txt_telefono.addKeyListener(this);
        delegado.txt_email.addKeyListener(this);
        delegado.tbl_contactos.addKeyListener(this);

        delegado.tbl_contactos.getSelectionModel().addListSelectionListener(this);

        delegado.tbl_contactos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = delegado.tbl_contactos.rowAtPoint(e.getPoint());

                if (row >= 0) {
                    delegado.tbl_contactos.setRowSelectionInterval(row, row);
                }

                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    cargarContactoSeleccionado();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mostrarPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mostrarPopup(e);
            }

            private void mostrarPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = delegado.tbl_contactos.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        delegado.tbl_contactos.setRowSelectionInterval(row, row);
                        delegado.popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        delegado.itemEditar.addActionListener(e -> cargarContactoSeleccionado());
        delegado.itemEliminar.addActionListener(e -> eliminarContacto());
    }

    private void incializacionCampos() {
        nombres = delegado.txt_nombre.getText().trim();
        email = delegado.txt_email.getText().trim();
        telefono = delegado.txt_telefono.getText().trim();
    }

    private void cargarContactosRegistrados() {
        synchronized (contactosLock) {
            try {
                contactos = new personaDAO(new persona()).leerArchivo();
                delegado.tableModel.setRowCount(0);

                for (persona contacto : contactos) {
                    delegado.tableModel.addRow(new Object[]{
                            contacto.getNombre(),
                            contacto.getTelefono(),
                            contacto.getEmail(),
                            contacto.getCategoria(),
                            contacto.isFavorito() ? "Sí" : "No"
                    });
                }
            } catch (IOException e) {
                contactos = new ArrayList<>();
                JOptionPane.showMessageDialog(delegado, "No se pudieron cargar los contactos.");
            }
        }
    }

    private void limpiarCampos() {
        delegado.txt_nombre.setText("");
        delegado.txt_telefono.setText("");
        delegado.txt_email.setText("");
        categoria = "";
        favorito = false;
        delegado.chb_favorito.setSelected(false);
        delegado.cmb_categoria.setSelectedIndex(0);
        delegado.tbl_contactos.clearSelection();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        incializacionCampos();

        if (e.getSource() == delegado.btn_add) {
            agregarContacto();
        } else if (e.getSource() == delegado.btn_eliminar) {
            eliminarContacto();
        } else if (e.getSource() == delegado.btn_modificar) {
            modificarContacto();
        } else if (e.getSource() == delegado.btn_exportar) {
            exportarCSV();
        }
    }

    // =========================
    // VALIDACIÓN EN SEGUNDO PLANO
    // =========================
    private void agregarContacto() {
        if (!validarCampos()) return;

        final String nombreValidado = nombres;
        final String telefonoValidado = telefono;
        final String emailValidado = email;
        final String categoriaValidada = categoria;
        final boolean favoritoValidado = favorito;

        delegado.btn_add.setEnabled(false);
        mostrarEstadoProceso("Validando contacto en segundo plano...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                synchronized (contactosLock) {
                    return contactoExistente(telefonoValidado);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean existe = get();

                    if (existe) {
                        finalizarProcesoConNotificacion("Contacto duplicado.");
                        JOptionPane.showMessageDialog(delegado, "Ya existe un contacto registrado con ese numero.");
                        return;
                    }

                    guardarContactoSincronizado(nombreValidado, telefonoValidado, emailValidado, categoriaValidada, favoritoValidado);
                    cargarContactosRegistrados();
                    limpiarCampos();
                    actualizarEstadisticas();
                    finalizarProcesoConNotificacion("Contacto registrado correctamente.");
                    JOptionPane.showMessageDialog(delegado, "Contacto registrado.");
                } catch (Exception ex) {
                    finalizarProcesoConNotificacion("Error al registrar contacto.");
                    JOptionPane.showMessageDialog(delegado, "Error al registrar el contacto.");
                    ex.printStackTrace();
                } finally {
                    delegado.btn_add.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void guardarContactoSincronizado(String nombre, String telefono, String email, String categoria, boolean favorito) {
        synchronized (contactosLock) {
            persona = new persona(nombre, telefono, email, categoria, favorito);
            new personaDAO(persona).escribirArchivo();
        }
    }

    private boolean contactoExistente(String telefonoValidado) {
        String telefonoLimpio = telefonoValidado.replaceAll("\\s+", "");

        for (persona p : contactos) {
            String telefonoRegistrado = p.getTelefono().replaceAll("\\s+", "");

            if (telefonoRegistrado.equalsIgnoreCase(telefonoLimpio)) {
                return true;
            }
        }

        return false;
    }

    // =========================
    // ELIMINACIÓN SINCRONIZADA
    // =========================
    private void eliminarContacto() {
        int selectedRow = delegado.tbl_contactos.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(delegado, "Seleccione un contacto para eliminar.");
            return;
        }

        int modelRow = delegado.tbl_contactos.convertRowIndexToModel(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(
                delegado,
                "¿Desea eliminar el contacto seleccionado?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            synchronized (contactosLock) {
                try {
                    contactos.remove(modelRow);
                    new personaDAO(new persona()).actualizarContactos(contactos);
                    cargarContactosRegistrados();
                    limpiarCampos();
                    actualizarEstadisticas();
                    finalizarProcesoConNotificacion("Contacto eliminado.");
                    JOptionPane.showMessageDialog(delegado, "Contacto eliminado.");
                } catch (IOException ex) {
                    finalizarProcesoConNotificacion("Error al eliminar contacto.");
                    JOptionPane.showMessageDialog(delegado, "Error al eliminar el contacto.");
                    ex.printStackTrace();
                }
            }
        }
    }

    // =========================
    // MODIFICACIÓN CON BLOQUEO DE RECURSO
    // =========================
    private void modificarContacto() {
        int selectedRow = delegado.tbl_contactos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(delegado, "Seleccione un contacto para modificar.");
            return;
        }

        if (!validarCampos()) return;

        if (!bloqueoEdicion.tryLock()) {
            JOptionPane.showMessageDialog(delegado, "El contacto está siendo editado por otro proceso. Intente nuevamente.");
            return;
        }

        try {
            int modelRow = delegado.tbl_contactos.convertRowIndexToModel(selectedRow);

            synchronized (contactosLock) {
                contactos.set(modelRow, new persona(nombres, telefono, email, categoria, favorito));
                new personaDAO(new persona()).actualizarContactos(contactos);
            }

            cargarContactosRegistrados();
            limpiarCampos();
            actualizarEstadisticas();
            finalizarProcesoConNotificacion("Contacto modificado.");
            JOptionPane.showMessageDialog(delegado, "Contacto modificado.");
        } catch (Exception e) {
            finalizarProcesoConNotificacion("No se pudo modificar contacto.");
            JOptionPane.showMessageDialog(delegado, "No se pudo modificar contacto.");
            e.printStackTrace();
        } finally {
            bloqueoEdicion.unlock();
        }
    }

    private void cargarContactoSeleccionado() {
        int selectedRow = delegado.tbl_contactos.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = delegado.tbl_contactos.convertRowIndexToModel(selectedRow);
        persona p = contactos.get(modelRow);

        delegado.txt_nombre.setText(p.getNombre());
        delegado.txt_telefono.setText(p.getTelefono());
        delegado.txt_email.setText(p.getEmail());
        delegado.chb_favorito.setSelected(p.isFavorito());
        delegado.cmb_categoria.setSelectedItem(p.getCategoria());
    }

    private boolean validarCampos() {
        if (nombres.isEmpty() || telefono.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(delegado, "Todos los campos deben ser llenados.");
            return false;
        }

        if (delegado.cmb_categoria.getSelectedIndex() == 0 || categoria.equals("")) {
            JOptionPane.showMessageDialog(delegado, "Elija una categoría.");
            return false;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(delegado, "Ingrese un email válido.");
            return false;
        }

        return true;
    }

    // =========================
    // EXPORTACIÓN CSV CONCURRENTE Y SINCRONIZADA
    // =========================
    private void exportarCSV() {
        if (contactos == null || contactos.isEmpty()) {
            JOptionPane.showMessageDialog(delegado, "No hay contactos para exportar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        int option = chooser.showSaveDialog(delegado);

        if (option == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String path = file.getAbsolutePath().endsWith(".csv")
                    ? file.getAbsolutePath()
                    : file.getAbsolutePath() + ".csv";

            List<persona> copiaContactos;
            synchronized (contactosLock) {
                copiaContactos = new ArrayList<>(contactos);
            }

            delegado.btn_exportar.setEnabled(false);
            mostrarEstadoProceso("Exportando CSV en segundo plano...");

            exportExecutor.submit(() -> {
                long inicio = System.currentTimeMillis();

                try {
                    escribirCSVSincronizado(path, copiaContactos);
                    long fin = System.currentTimeMillis();
                    long tiempo = fin - inicio;

                    SwingUtilities.invokeLater(() -> {
                        finalizarProcesoConNotificacion("Exportación completada en " + tiempo + " ms.");
                        JOptionPane.showMessageDialog(delegado,
                                "CSV exportado correctamente.\nTiempo de exportación: " + tiempo + " ms.");
                    });
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> {
                        finalizarProcesoConNotificacion("Error al exportar CSV.");
                        JOptionPane.showMessageDialog(delegado, "Error al exportar CSV.");
                    });
                } finally {
                    SwingUtilities.invokeLater(() -> delegado.btn_exportar.setEnabled(true));
                }
            });
        }
    }

    private void escribirCSVSincronizado(String path, List<persona> contactosExportar) throws IOException {
        synchronized (exportLock) {
            try (FileWriter writer = new FileWriter(path)) {
                writer.write("Nombre,Telefono,Email,Categoria,Favorito\n");

                for (persona p : contactosExportar) {
                    writer.write(p.getNombre() + "," +
                            p.getTelefono() + "," +
                            p.getEmail() + "," +
                            p.getCategoria() + "," +
                            (p.isFavorito() ? "Sí" : "No") + "\n");
                }
            }
        }
    }

    private void actualizarEstadisticas() {
        int total = contactos != null ? contactos.size() : 0;
        int favoritos = 0;

        if (contactos != null) {
            for (persona p : contactos) {
                if (p.isFavorito()) {
                    favoritos++;
                }
            }
        }

        // Método de la vista moderna con internacionalización.
        delegado.actualizarLabelsEstadisticas(total, favoritos);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            cargarContactoSeleccionado();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == delegado.cmb_categoria) {
            Object seleccionado = delegado.cmb_categoria.getSelectedItem();

            if (seleccionado != null) {
                categoria = seleccionado.toString();
            } else {
                categoria = "";
            }

        } else if (e.getSource() == delegado.chb_favorito) {
            favorito = delegado.chb_favorito.isSelected();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_N) {
            agregarContacto();
        } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
            exportarCSV();
        } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            eliminarContacto();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getSource() == delegado.txt_buscar) {
            buscarContactosEnSegundoPlano(delegado.txt_buscar.getText().trim());
        }
    }

    // =========================
    // BÚSQUEDA EN SEGUNDO PLANO
    // =========================
    private void buscarContactosEnSegundoPlano(String texto) {
        if (busquedaWorker != null && !busquedaWorker.isDone()) {
            busquedaWorker.cancel(true);
        }

        mostrarEstadoProceso("Buscando contactos...");

        busquedaWorker = new SwingWorker<RowFilter<DefaultTableModel, Object>, Void>() {
            @Override
            protected RowFilter<DefaultTableModel, Object> doInBackground() throws Exception {
                Thread.sleep(150);

                if (isCancelled()) return null;

                if (texto.isEmpty()) {
                    return null;
                }

                return RowFilter.regexFilter("(?i)" + Pattern.quote(texto), 0);
            }

            @Override
            protected void done() {
                if (isCancelled()) return;

                try {
                    RowFilter<DefaultTableModel, Object> filtro = get();
                    sorter.setRowFilter(filtro);
                    finalizarProcesoConNotificacion(texto.isEmpty() ? "Búsqueda limpia." : "Búsqueda realizada en segundo plano.");
                } catch (Exception ex) {
                    sorter.setRowFilter(null);
                    finalizarProcesoConNotificacion("Error en la búsqueda.");
                }
            }
        };

        busquedaWorker.execute();
    }

    // =========================
    // NOTIFICACIONES EN LA INTERFAZ CON SUBPROCESOS
    // =========================
    private void mostrarEstadoProceso(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            delegado.progressBar.setIndeterminate(true);
            delegado.progressBar.setString(mensaje);
        });
    }

    private void finalizarProcesoConNotificacion(String mensaje) {
        Thread notificacionThread = new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                delegado.progressBar.setIndeterminate(false);
                delegado.progressBar.setValue(100);
                delegado.progressBar.setString(mensaje);
            });

            try {
                Thread.sleep(1800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            SwingUtilities.invokeLater(() -> {
                delegado.progressBar.setValue(0);
                delegado.progressBar.setString("0 %");
            });
        });

        notificacionThread.start();
    }
}
