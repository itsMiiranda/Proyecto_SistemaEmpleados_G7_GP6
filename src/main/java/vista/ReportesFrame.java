package vista;

import dao.EmpleadoDAO;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import servicio.ReporteService;
import servicio.TablaReporte;
import util.ExportadorCSV;

/**
 * Proceso 4: generación de reportes (listados, remuneraciones, resumen e
 * historial) con opción de exportar a CSV.
 *
 * @author G7_GP6
 */
public class ReportesFrame extends JFrame {

    private static final String R_GENERAL = "Listado general de empleados";
    private static final String R_POR_TIPO = "Listado por tipo de empleado";
    private static final String R_INDIVIDUAL = "Remuneraciones individuales";
    private static final String R_RESUMEN = "Resumen de remuneraciones";
    private static final String R_HISTORIAL = "Historial de cálculos";
    private static final String TODOS = "TODOS";

    private final ReporteService servicio = new ReporteService();

    private JComboBox<String> cmbReporte;
    private JComboBox<String> cmbTipo;
    private JCheckBox chkInactivos;
    private JTextField txtCodigo;
    private JTextField txtPeriodo;

    private JTable tabla;
    private JScrollPane scrollTabla;
    private DefaultTableModel modeloTabla;
    private JLabel lblTitulo;
    private JLabel lblPie;

    private TablaReporte reporteActual;

    public ReportesFrame() {
        setTitle("Reportes e historial de remuneraciones");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        raiz.add(crearPanelFiltros(), BorderLayout.NORTH);
        raiz.add(crearPanelTabla(), BorderLayout.CENTER);
        setContentPane(raiz);

        actualizarFiltros();
    }

    // ------------------------------------------------------------------
    // Interfaz
    // ------------------------------------------------------------------

    private JPanel crearPanelFiltros() {
        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Reporte y filtros"));

        cmbReporte = new JComboBox<>(new String[]{R_GENERAL, R_POR_TIPO, R_INDIVIDUAL, R_RESUMEN, R_HISTORIAL});
        cmbTipo = new JComboBox<>(new String[]{TODOS, EmpleadoDAO.ADMINISTRATIVO,
            EmpleadoDAO.POR_HORA, EmpleadoDAO.POR_COMISION});
        chkInactivos = new JCheckBox("Incluir inactivos", true);
        txtCodigo = new JTextField(10);
        txtCodigo.setToolTipText("Vacío = todos los empleados");
        txtPeriodo = new JTextField(8);
        txtPeriodo.setToolTipText("aaaa-mm; vacío = todos los períodos");

        JButton btnGenerar = new JButton("Generar reporte");
        JButton btnExportar = new JButton("Exportar a CSV");

        cmbReporte.addActionListener(e -> actualizarFiltros());
        btnGenerar.addActionListener(e -> generar());
        btnExportar.addActionListener(e -> exportar());

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila1.add(new JLabel("Reporte:"));
        fila1.add(cmbReporte);
        fila1.add(new JLabel("Tipo:"));
        fila1.add(cmbTipo);
        fila1.add(chkInactivos);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila2.add(new JLabel("Código empleado:"));
        fila2.add(txtCodigo);
        fila2.add(new JLabel("Período (aaaa-mm):"));
        fila2.add(txtPeriodo);
        fila2.add(btnGenerar);
        fila2.add(btnExportar);

        panel.add(fila1);
        panel.add(fila2);
        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));

        lblTitulo = new JLabel("Seleccione un reporte y pulse \"Generar reporte\".");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(lblTitulo, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(24);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD));
        scrollTabla = new JScrollPane(tabla);
        panel.add(scrollTabla, BorderLayout.CENTER);

        lblPie = new JLabel(" ");
        lblPie.setFont(lblPie.getFont().deriveFont(Font.BOLD));
        panel.add(lblPie, BorderLayout.SOUTH);
        return panel;
    }

    /** Habilita solo los filtros que aplican al reporte elegido. */
    private void actualizarFiltros() {
        String r = (String) cmbReporte.getSelectedItem();
        boolean porTipo = R_POR_TIPO.equals(r) || R_INDIVIDUAL.equals(r) || R_RESUMEN.equals(r);
        boolean inactivos = R_GENERAL.equals(r) || R_POR_TIPO.equals(r);
        boolean historial = R_HISTORIAL.equals(r);

        cmbTipo.setEnabled(porTipo);
        chkInactivos.setEnabled(inactivos);
        txtCodigo.setEnabled(historial);
        txtPeriodo.setEnabled(historial);
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    private void generar() {
        String r = (String) cmbReporte.getSelectedItem();
        String tipo = TODOS.equals(cmbTipo.getSelectedItem()) ? null : (String) cmbTipo.getSelectedItem();

        try {
            TablaReporte t;
            if (R_GENERAL.equals(r)) {
                t = servicio.listadoGeneral(chkInactivos.isSelected());
            } else if (R_POR_TIPO.equals(r)) {
                t = servicio.listadoPorTipo(tipo, chkInactivos.isSelected());
            } else if (R_INDIVIDUAL.equals(r)) {
                t = servicio.remuneracionesIndividuales(tipo);
            } else if (R_RESUMEN.equals(r)) {
                t = servicio.resumenRemuneraciones(tipo);
            } else {
                t = servicio.historial(txtCodigo.getText(), txtPeriodo.getText());
            }
            mostrar(t);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo generar el reporte (error de conexión, lectura o escritura en MongoDB).\n"
                    + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrar(TablaReporte t) {
        reporteActual = t;
        modeloTabla.setColumnIdentifiers(t.getColumnas());
        modeloTabla.setRowCount(0);
        for (Object[] fila : t.getFilas()) {
            modeloTabla.addRow(fila);
        }
        ajustarColumnas();
        lblTitulo.setText(t.getTitulo());
        lblPie.setText(t.getPie());

        if (t.estaVacia()) {
            JOptionPane.showMessageDialog(this, t.getPie(), "Consulta sin resultados",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Ajusta el ancho de cada columna a su contenido; si no caben, aparece la barra horizontal. */
    private void ajustarColumnas() {
        int total = 0;
        for (int c = 0; c < tabla.getColumnCount(); c++) {
            TableColumn col = tabla.getColumnModel().getColumn(c);
            Component encabezado = tabla.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(tabla, col.getHeaderValue(), false, false, -1, c);
            int ancho = encabezado.getPreferredSize().width;
            for (int f = 0; f < tabla.getRowCount(); f++) {
                Component celda = tabla.prepareRenderer(tabla.getCellRenderer(f, c), f, c);
                ancho = Math.max(ancho, celda.getPreferredSize().width);
            }
            ancho = Math.min(ancho + 10, 460);
            col.setPreferredWidth(ancho);
            col.setWidth(ancho);
            total += ancho;
        }
        tabla.setAutoResizeMode(total > scrollTabla.getViewport().getWidth()
                ? JTable.AUTO_RESIZE_OFF : JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void exportar() {
        if (reporteActual == null || reporteActual.estaVacia()) {
            JOptionPane.showMessageDialog(this, "Primero genere un reporte con resultados para exportar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Exportar reporte a CSV");
        selector.setFileFilter(new FileNameExtensionFilter("Archivo CSV (*.csv)", "csv"));
        selector.setSelectedFile(new File("reporte.csv"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".csv");
        }
        if (archivo.exists() && JOptionPane.showConfirmDialog(this,
                "El archivo ya existe. ¿Desea reemplazarlo?", "Confirmar",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            ExportadorCSV.exportar(archivo, reporteActual.getColumnas(), reporteActual.getFilas());
            JOptionPane.showMessageDialog(this, "Reporte exportado a:\n" + archivo.getAbsolutePath(),
                    "Exportación completada", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo escribir el archivo.\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
