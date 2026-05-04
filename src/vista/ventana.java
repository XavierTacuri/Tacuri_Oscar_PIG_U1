//Autor Xavier Tacuri

package vista;

import controlador.logica_ventana;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Locale;
import java.util.ResourceBundle;

public class ventana extends JFrame {

    public JPanel contentPane;
    public JTextField txt_nombre;
    public JTextField txt_telefono;
    public JTextField txt_email;
    public JTextField txt_buscar;
    public JCheckBox chb_favorito;
    public JComboBox<String> cmb_categoria;
    public JComboBox<String> cmb_idioma;
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

    private JLabel lblTitulo;
    private JLabel lblSubtitulo;
    private JLabel lblNombre;
    private JLabel lblTelefono;
    private JLabel lblEmail;
    private JLabel lblFavorito;
    private JLabel lblCategoria;
    private JLabel lblTituloFormulario;
    private JLabel lblTituloAcciones;
    private JLabel lblTituloTabla;
    private JLabel lblBuscar;
    private JLabel lblTituloBusqueda;

    private ResourceBundle textos;
    private Locale localeActual = new Locale("es");

    private final Color FONDO = new Color(248, 250, 252);
    private final Color CARD = Color.WHITE;
    private final Color BORDE = new Color(226, 232, 240);
    private final Color TEXTO = new Color(15, 23, 42);
    private final Color SECUNDARIO = new Color(100, 116, 139);
    private final Color AZUL = new Color(37, 99, 235);

    private final Font FUENTE = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FUENTE_NEGRITA = new Font("Segoe UI", Font.BOLD, 13);

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
        cargarTextos(localeActual);

        setTitle(t("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 620);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
        setResizable(true);

        aplicarLookAndFeel();

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        setContentPane(tabbedPane);

        construirPestanaContactos();
        construirPestanaEstadisticas();
        construirPopupMenu();
        aplicarTextosInterfaz();

        new logica_ventana(this);
    }

    private void aplicarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private void cargarTextos(Locale locale) {
        textos = ResourceBundle.getBundle("vista.messages", locale);
    }

    public String t(String clave) {
        return textos.getString(clave);
    }

    public Locale getLocaleActual() {
        return localeActual;
    }

    private void construirPestanaContactos() {
        contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(FONDO);
        contentPane.setBorder(new EmptyBorder(10, 12, 10, 12));
        tabbedPane.addTab("", contentPane);

        contentPane.add(crearHeaderCompacto(), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(10, 10));
        cuerpo.setOpaque(false);

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setOpaque(false);
        panelSuperior.setPreferredSize(new Dimension(0, 200));
        panelSuperior.add(crearPanelFormulario(), BorderLayout.CENTER);
        panelSuperior.add(crearPanelBotones(), BorderLayout.EAST);

        cuerpo.add(panelSuperior, BorderLayout.NORTH);
        cuerpo.add(crearPanelTabla(), BorderLayout.CENTER);
        cuerpo.add(crearPanelInferior(), BorderLayout.SOUTH);

        contentPane.add(cuerpo, BorderLayout.CENTER);
    }

    private JPanel crearHeaderCompacto() {
        JPanel header = crearCard(8, 14);
        header.setLayout(new BorderLayout(10, 0));
        header.setPreferredSize(new Dimension(0, 72));

        JPanel textosPanel = new JPanel(new GridLayout(2, 1));
        textosPanel.setOpaque(false);

        lblTitulo = new JLabel();
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(AZUL);

        lblSubtitulo = new JLabel();
        lblSubtitulo.setFont(FUENTE);
        lblSubtitulo.setForeground(SECUNDARIO);

        textosPanel.add(lblTitulo);
        textosPanel.add(lblSubtitulo);

        cmb_idioma = new JComboBox<>(new String[]{"Español", "English", "Português"});
        cmb_idioma.setFont(FUENTE_NEGRITA);
        cmb_idioma.setPreferredSize(new Dimension(130, 32));
        cmb_idioma.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                SwingUtilities.invokeLater(this::cambiarIdioma);
            }
        });

        header.add(textosPanel, BorderLayout.CENTER);
        header.add(cmb_idioma, BorderLayout.EAST);

        return header;
    }

    private JPanel crearPanelFormulario() {
        JPanel card = crearCard(8, 12);
        card.setLayout(new BorderLayout(0, 6));

        lblTituloFormulario = crearTituloCard("");
        card.add(lblTituloFormulario, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txt_nombre = crearCampoTexto();
        txt_telefono = crearCampoTexto();
        txt_email = crearCampoTexto();

        lblNombre = crearLabel("");
        lblTelefono = crearLabel("");
        lblEmail = crearLabel("");
        lblFavorito = crearLabel("");
        lblCategoria = crearLabel("");

        agregarFila(form, gbc, 0, lblNombre, txt_nombre);
        agregarFila(form, gbc, 1, lblTelefono, txt_telefono);
        agregarFila(form, gbc, 2, lblEmail, txt_email);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        form.add(lblFavorito, gbc);

        chb_favorito = new JCheckBox();
        chb_favorito.setOpaque(false);
        chb_favorito.setFont(FUENTE);
        chb_favorito.setForeground(TEXTO);
        chb_favorito.setPreferredSize(new Dimension(260, 28));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1;
        form.add(chb_favorito, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        form.add(lblCategoria, gbc);

        cmb_categoria = new JComboBox<>();
        cmb_categoria.setFont(FUENTE);
        cmb_categoria.setPreferredSize(new Dimension(520, 28));
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1;
        form.add(cmb_categoria, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private void agregarFila(JPanel panel, GridBagConstraints gbc, int fila, JLabel label, JTextField campo) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = fila;
        gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    private JPanel crearPanelBotones() {
        JPanel card = crearCard(8, 12);
        card.setLayout(new BorderLayout(0, 8));
        card.setPreferredSize(new Dimension(185, 0));

        lblTituloAcciones = crearTituloCard("");
        card.add(lblTituloAcciones, BorderLayout.NORTH);

        JPanel botones = new JPanel(new GridLayout(4, 1, 0, 8));
        botones.setOpaque(false);

        btn_add = crearBoton("", new Color(37, 99, 235), Color.WHITE);
        btn_modificar = crearBoton("", new Color(234, 179, 8), TEXTO);
        btn_eliminar = crearBoton("", new Color(239, 68, 68), Color.WHITE);
        btn_exportar = crearBoton("", new Color(34, 197, 94), Color.WHITE);

        botones.add(btn_add);
        botones.add(btn_modificar);
        botones.add(btn_eliminar);
        botones.add(btn_exportar);

        card.add(botones, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearPanelTabla() {
        JPanel card = crearCard(8, 12);
        card.setLayout(new BorderLayout(0, 8));

        lblTituloTabla = crearTituloCard("");
        card.add(lblTituloTabla, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"", "", "", "", ""}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tbl_contactos = new JTable(tableModel);
        tbl_contactos.setFont(FUENTE);
        tbl_contactos.setRowHeight(25);
        tbl_contactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_contactos.setAutoCreateRowSorter(true);
        tbl_contactos.setGridColor(BORDE);
        tbl_contactos.setShowVerticalLines(true);
        tbl_contactos.setFillsViewportHeight(true);
        tbl_contactos.setSelectionBackground(new Color(219, 234, 254));
        tbl_contactos.setSelectionForeground(TEXTO);

        JTableHeader header = tbl_contactos.getTableHeader();
        header.setFont(FUENTE_NEGRITA);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXTO);
        header.setPreferredSize(new Dimension(0, 28));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 6, 0, 6));
        tbl_contactos.setDefaultRenderer(Object.class, renderer);

        scrTabla = new JScrollPane(tbl_contactos);
        scrTabla.setBorder(new LineBorder(BORDE, 1));
        scrTabla.setPreferredSize(new Dimension(0, 190));

        card.add(scrTabla, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(10, 8));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 76));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(0, 20));
        progressBar.setFont(FUENTE_NEGRITA);

        JPanel busqueda = crearCard(8, 12);
        busqueda.setLayout(new BorderLayout(8, 0));
        busqueda.setPreferredSize(new Dimension(0, 42));

        lblBuscar = crearLabel("");
        lblBuscar.setPreferredSize(new Dimension(135, 28));
        txt_buscar = crearCampoTexto();

        busqueda.add(lblBuscar, BorderLayout.WEST);
        busqueda.add(txt_buscar, BorderLayout.CENTER);

        panel.add(progressBar, BorderLayout.NORTH);
        panel.add(busqueda, BorderLayout.CENTER);

        return panel;
    }

    private void construirPestanaEstadisticas() {
        JPanel panelEstadisticas = new JPanel(new GridLayout(2, 1, 12, 12));
        panelEstadisticas.setBackground(FONDO);
        panelEstadisticas.setBorder(new EmptyBorder(30, 30, 30, 30));

        lbl_total = new JLabel("", SwingConstants.CENTER);
        lbl_total.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lbl_total.setForeground(AZUL);

        lbl_favoritos = new JLabel("", SwingConstants.CENTER);
        lbl_favoritos.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        lbl_favoritos.setForeground(TEXTO);

        panelEstadisticas.add(lbl_total);
        panelEstadisticas.add(lbl_favoritos);

        tabbedPane.addTab("", panelEstadisticas);
    }

    private void construirPopupMenu() {
        popupMenu = new JPopupMenu();
        itemEditar = new JMenuItem();
        itemEliminar = new JMenuItem();
        popupMenu.add(itemEditar);
        popupMenu.add(itemEliminar);
    }

    private void cambiarIdioma() {


        int index = cmb_idioma.getSelectedIndex();
        if (index == 0) {
            localeActual = new Locale("es");
        } else if (index == 1) {
            localeActual = new Locale("en");
        } else {
            localeActual = new Locale("pt");
        }
        cargarTextos(localeActual);
        aplicarTextosInterfaz();
        revalidate();
        repaint();
    }

    public void aplicarTextosInterfaz() {
        setTitle(t("app.title"));
        lblTitulo.setText(t("app.title"));
        lblSubtitulo.setText(t("app.subtitle"));

        tabbedPane.setTitleAt(0, t("tab.contacts"));
        tabbedPane.setTitleAt(1, t("tab.stats"));

        lblTituloFormulario.setText(t("form.title"));
        lblNombre.setText(t("label.name"));
        lblTelefono.setText(t("label.phone"));
        lblEmail.setText(t("label.email"));
        lblFavorito.setText(t("label.favorite"));
        chb_favorito.setText(t("checkbox.favorite"));
        lblCategoria.setText(t("label.category"));

        String categoriaSeleccionada = cmb_categoria.getSelectedItem() != null ? cmb_categoria.getSelectedItem().toString() : "";
        cmb_categoria.removeAllItems();
        cmb_categoria.addItem(t("category.select"));
        cmb_categoria.addItem(t("category.family"));
        cmb_categoria.addItem(t("category.friends"));
        cmb_categoria.addItem(t("category.work"));
        if (categoriaSeleccionada.equalsIgnoreCase("Familia") || categoriaSeleccionada.equalsIgnoreCase("Family") || categoriaSeleccionada.equalsIgnoreCase("Família")) {
            cmb_categoria.setSelectedIndex(1);
        } else if (categoriaSeleccionada.equalsIgnoreCase("Amigos") || categoriaSeleccionada.equalsIgnoreCase("Friends")) {
            cmb_categoria.setSelectedIndex(2);
        } else if (categoriaSeleccionada.equalsIgnoreCase("Trabajo") || categoriaSeleccionada.equalsIgnoreCase("Work") || categoriaSeleccionada.equalsIgnoreCase("Trabalho")) {
            cmb_categoria.setSelectedIndex(3);
        } else {
            cmb_categoria.setSelectedIndex(0);
        }

        lblTituloAcciones.setText(t("actions.title"));
        btn_add.setText(t("button.add"));
        btn_modificar.setText(t("button.edit"));
        btn_eliminar.setText(t("button.delete"));
        btn_exportar.setText(t("button.export"));

        lblTituloTabla.setText(t("table.title"));
        tableModel.setColumnIdentifiers(new Object[]{
                t("table.name"), t("table.phone"), t("table.email"), t("table.category"), t("table.favorite")
        });

        lblBuscar.setText(t("search.name"));
        itemEditar.setText(t("popup.edit"));
        itemEliminar.setText(t("popup.delete"));

        actualizarLabelsEstadisticas(0, 0);
    }

    public void actualizarLabelsEstadisticas(int total, int favoritos) {
        lbl_total.setText(t("stats.total") + ": " + total);
        lbl_favoritos.setText(t("stats.favorites") + ": " + favoritos);
    }

    private JPanel crearCard(int vertical, int horizontal) {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDE, 1),
                new EmptyBorder(vertical, horizontal, vertical, horizontal)
        ));
        return panel;
    }

    private JLabel crearTituloCard(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(TEXTO);
        return label;
    }

    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FUENTE_NEGRITA);
        label.setForeground(TEXTO);
        label.setPreferredSize(new Dimension(100, 26));
        return label;
    }

    private JTextField crearCampoTexto() {
        JTextField campo = new JTextField();
        campo.setFont(FUENTE);
        campo.setForeground(TEXTO);
        campo.setPreferredSize(new Dimension(520, 28));
        campo.setMinimumSize(new Dimension(220, 28));
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDE, 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
        return campo;
    }

    private JButton crearBoton(String texto, Color fondo, Color colorTexto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setForeground(colorTexto);
        boton.setBackground(fondo);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(150, 34));
        return boton;
    }
}
