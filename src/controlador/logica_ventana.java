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

public class logica_ventana implements ActionListener, ListSelectionListener, ItemListener, KeyListener {
    private ventana delegado;
    private String nombres, email, telefono, categoria = "";
    private persona persona;
    private List<persona> contactos;
    private boolean favorito = false;
    private TableRowSorter<DefaultTableModel> sorter;

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

    private void agregarContacto() {
        if (!validarCampos()) return;
        System.out.println("Agregando contacto...");
        persona = new persona(nombres, telefono, email, categoria, favorito);
        new personaDAO(persona).escribirArchivo();
        cargarContactosRegistrados();
        limpiarCampos();
        actualizarEstadisticas();
        JOptionPane.showMessageDialog(delegado, "Contacto registrado.");
    }

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
            contactos.remove(modelRow);

            try {
                new personaDAO(new persona()).actualizarContactos(contactos);
                cargarContactosRegistrados();
                limpiarCampos();
                actualizarEstadisticas();
                JOptionPane.showMessageDialog(delegado, "Contacto eliminado.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(delegado, "Error al eliminar el contacto.");
                ex.printStackTrace();
            }
        }
    }

    private void modificarContacto() {
        int selectedRow = delegado.tbl_contactos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(delegado, "Seleccione un contacto para modificar.");
            return;
        }

        if (!validarCampos()) return;

        int modelRow = delegado.tbl_contactos.convertRowIndexToModel(selectedRow);
        contactos.set(modelRow, new persona(nombres, telefono, email, categoria, favorito));

        try{
            new personaDAO(new persona()).actualizarContactos(contactos);
            cargarContactosRegistrados();
            limpiarCampos();
            actualizarEstadisticas();
            JOptionPane.showMessageDialog(delegado, "Contacto modificado.");
        }catch (Exception e){
            JOptionPane.showMessageDialog(delegado, "No se pudo modificar contacto.");
            e.printStackTrace();
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

        if (categoria.equals("") || categoria.equals("Elija una Categoria")) {
            JOptionPane.showMessageDialog(delegado, "Elija una categoría.");
            return false;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(delegado, "Ingrese un email válido.");
            return false;
        }

        return true;
    }

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

            try (FileWriter writer = new FileWriter(path)) {
                writer.write("Nombre,Telefono,Email,Categoria,Favorito\n");

                for (persona p : contactos) {
                    writer.write(p.getNombre() + "," +
                            p.getTelefono() + "," +
                            p.getEmail() + "," +
                            p.getCategoria() + "," +
                            (p.isFavorito() ? "Sí" : "No") + "\n");
                }

                JOptionPane.showMessageDialog(delegado, "CSV exportado correctamente.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(delegado, "Error al exportar CSV.");
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

        delegado.lbl_total.setText("Total de contactos: " + total);
        delegado.lbl_favoritos.setText("Contactos favoritos: " + favoritos);
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
            categoria = delegado.cmb_categoria.getSelectedItem().toString();
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
            String texto = delegado.txt_buscar.getText().trim();
            if (texto.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 0));
            }
        }
    }
}