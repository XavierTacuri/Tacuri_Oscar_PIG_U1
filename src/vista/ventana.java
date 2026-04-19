//Autor Xavier Tacuri

package vista;

import controlador.logica_ventana;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ventana extends JFrame {

    public JPanel contentPane;
    public JTextField txt_nombre;
    public JTextField txt_telefono;
    public JTextField txt_email;
    public JTextField txt_buscar;
    public JCheckBox chb_favorito;
    public JComboBox<String> cmb_categoria;
    public JButton btn_add;
    public JButton btn_modificar;
    public JButton btn_eliminar;
    public JButton btn_exportar;

    public JTable tbl_contactos;
    public JScrollPane scrTabla;
    public JProgressBar progressBar;
    public JLabel lbl_total;
    public JLabel lbl_favoritos;

    public JPopupMenu popupMenu;
    public JMenuItem itemEditar;
    public JMenuItem itemEliminar;

    public DefaultTableModel tableModel;
    public JTabbedPane tabbedPane;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ventana frame = new ventana();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ventana() {
        setTitle("GESTION DE CONTACTOS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(820, 560));

        tabbedPane = new JTabbedPane();
        setContentPane(tabbedPane);

        construirPestanaContactos();
        construirPestanaEstadisticas();
        construirPopupMenu();

        new logica_ventana(this);
    }

    private void construirPestanaContactos() {
        contentPane = new JPanel(new BorderLayout(12, 12));
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        tabbedPane.addTab("Contactos", contentPane);

        JPanel panelSuperior = new JPanel(new BorderLayout(12, 12));
        contentPane.add(panelSuperior, BorderLayout.NORTH);

        panelSuperior.add(crearPanelFormulario(), BorderLayout.CENTER);
        panelSuperior.add(crearPanelBotones(), BorderLayout.EAST);

        JPanel panelCentro = new JPanel(new BorderLayout(8, 8));
        panelCentro.setBorder(new TitledBorder("Lista de contactos"));

        tableModel = new DefaultTableModel(
                new Object[]{"Nombre", "Telefono", "Email", "Categoria", "Favorito"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tbl_contactos = new JTable(tableModel);
        tbl_contactos.setRowHeight(24);
        tbl_contactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_contactos.setAutoCreateRowSorter(true);

        scrTabla = new JScrollPane(tbl_contactos);
        panelCentro.add(scrTabla, BorderLayout.CENTER);

        contentPane.add(panelCentro, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout(8, 8));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        JPanel panelBusqueda = new JPanel(new BorderLayout(8, 8));
        JLabel lblBuscar = new JLabel("Buscar por nombre:");
        lblBuscar.setFont(new Font("Tahoma", Font.BOLD, 14));

        txt_buscar = new JTextField();

        panelBusqueda.add(lblBuscar, BorderLayout.WEST);
        panelBusqueda.add(txt_buscar, BorderLayout.CENTER);

        panelInferior.add(progressBar, BorderLayout.NORTH);
        panelInferior.add(panelBusqueda, BorderLayout.SOUTH);

        contentPane.add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelFormulario() {
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(new TitledBorder("Datos del contacto"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblNombres = new JLabel("Nombre:");
        lblNombres.setFont(new Font("Tahoma", Font.BOLD, 15));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panelFormulario.add(lblNombres, gbc);

        txt_nombre = new JTextField(25);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        panelFormulario.add(txt_nombre, gbc);

        JLabel lblTelefono = new JLabel("Telefono:");
        lblTelefono.setFont(new Font("Tahoma", Font.BOLD, 15));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panelFormulario.add(lblTelefono, gbc);

        txt_telefono = new JTextField(25);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        panelFormulario.add(txt_telefono, gbc);

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Tahoma", Font.BOLD, 15));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panelFormulario.add(lblEmail, gbc);

        txt_email = new JTextField(25);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        panelFormulario.add(txt_email, gbc);

        chb_favorito = new JCheckBox("CONTACTO FAVORITO");
        chb_favorito.setFont(new Font("Tahoma", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panelFormulario.add(chb_favorito, gbc);

        cmb_categoria = new JComboBox<>();
        cmb_categoria.addItem("Elija una Categoria");
        cmb_categoria.addItem("Familia");
        cmb_categoria.addItem("Amigos");
        cmb_categoria.addItem("Trabajo");

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1;
        panelFormulario.add(cmb_categoria, gbc);

        return panelFormulario;
    }

    private JPanel crearPanelBotones() {
        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 8, 8));
        panelBotones.setBorder(new TitledBorder("Acciones"));
        panelBotones.setPreferredSize(new Dimension(190, 0));

        btn_add = new JButton("AGREGAR");
        btn_modificar = new JButton("MODIFICAR");
        btn_eliminar = new JButton("ELIMINAR");
        btn_exportar = new JButton("EXPORTAR CSV");

        Font fontBoton = new Font("Tahoma", Font.PLAIN, 14);
        btn_add.setFont(fontBoton);
        btn_modificar.setFont(fontBoton);
        btn_eliminar.setFont(fontBoton);
        btn_exportar.setFont(fontBoton);

        panelBotones.add(btn_add);
        panelBotones.add(btn_modificar);
        panelBotones.add(btn_eliminar);
        panelBotones.add(btn_exportar);

        return panelBotones;
    }

    private void construirPestanaEstadisticas() {
        JPanel panelEstadisticas = new JPanel(new GridLayout(2, 1, 12, 12));
        panelEstadisticas.setBorder(new EmptyBorder(30, 30, 30, 30));

        lbl_total = new JLabel("Total de contactos: 0", SwingConstants.CENTER);
        lbl_total.setFont(new Font("Tahoma", Font.BOLD, 24));

        lbl_favoritos = new JLabel("Contactos favoritos: 0", SwingConstants.CENTER);
        lbl_favoritos.setFont(new Font("Tahoma", Font.PLAIN, 22));

        panelEstadisticas.add(lbl_total);
        panelEstadisticas.add(lbl_favoritos);

        tabbedPane.addTab("Estadísticas", panelEstadisticas);
    }

    private void construirPopupMenu() {
        popupMenu = new JPopupMenu();
        itemEditar = new JMenuItem("Editar");
        itemEliminar = new JMenuItem("Eliminar");
        popupMenu.add(itemEditar);
        popupMenu.add(itemEliminar);
    }
}