package vista;

import dao.EmpleadoDAO;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import modelo.Calculo;
import modelo.Empleado;
import servicio.CalculoException;
import servicio.CalculoService;

/**
 * Proceso 2 (cálculo de remuneraciones) y registro del resultado (Proceso 5).
 *
 * Flujo: seleccionar empleado -> Calcular (vista previa, no guarda) ->
 * Registrar resultado (vuelve a calcular desde MongoDB y guarda en el historial).
 * El importe nunca se escribe a mano: siempre sale de los datos registrados.
 *
 * @author G7_GP6
 */
public class CalculosFrame extends JFrame {

    private static final String SELECCIONE = "-- Seleccione un empleado --";

    private final CalculoService servicio = new CalculoService();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    private JComboBox<String> cmbEmpleados;
    private JTextField txtCodigo;
    private JTextField txtPeriodo;
    private JButton btnCalcular;
    private JButton btnRegistrar;
    private JTextArea areaResultado;
    private JLabel lblEstado;

    // Códigos en el mismo orden que el combo (la posición 0 es el texto "Seleccione")
    private final List<String> codigosCombo = new ArrayList<>();

    public CalculosFrame() {
        setTitle("Cálculo de remuneraciones");
        setSize(720, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        raiz.add(crearPanelSeleccion(), BorderLayout.NORTH);
        raiz.add(crearPanelResultado(), BorderLayout.CENTER);
        setContentPane(raiz);

        cargarEmpleados();
        limpiarResultado();
    }

    // ------------------------------------------------------------------
    // Interfaz
    // ------------------------------------------------------------------

    private JPanel crearPanelSeleccion() {
        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("1. Seleccionar empleado y período"));

        cmbEmpleados = new JComboBox<>();
        cmbEmpleados.addActionListener(e -> {
            int i = cmbEmpleados.getSelectedIndex();
            if (i > 0 && i < codigosCombo.size()) {
                txtCodigo.setText(codigosCombo.get(i));
            }
        });

        txtCodigo = new JTextField(12);
        txtCodigo.setToolTipText("Código del empleado, por ejemplo EMP-001");
        txtPeriodo = new JTextField(8);
        txtPeriodo.setToolTipText("Formato aaaa-mm");
        txtPeriodo.setText(YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));

        DocumentListener alCambiar = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                limpiarResultado();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                limpiarResultado();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                limpiarResultado();
            }
        };
        txtCodigo.getDocument().addDocumentListener(alCambiar);
        txtPeriodo.getDocument().addDocumentListener(alCambiar);

        btnCalcular = new JButton("Calcular");
        btnRegistrar = new JButton("Registrar resultado");
        JButton btnLimpiar = new JButton("Limpiar");
        btnCalcular.addActionListener(e -> calcular());
        btnRegistrar.addActionListener(e -> registrar());
        btnLimpiar.addActionListener(e -> {
            cmbEmpleados.setSelectedIndex(0);
            txtCodigo.setText("");
            limpiarResultado();
        });

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila1.add(new JLabel("Lista de empleados:"));
        fila1.add(cmbEmpleados);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila2.add(new JLabel("Código:"));
        fila2.add(txtCodigo);
        fila2.add(new JLabel("Período (aaaa-mm):"));
        fila2.add(txtPeriodo);

        JPanel fila3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila3.add(btnCalcular);
        fila3.add(btnRegistrar);
        fila3.add(btnLimpiar);

        panel.add(fila1);
        panel.add(fila2);
        panel.add(fila3);
        return panel;
    }

    private JPanel crearPanelResultado() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("2. Resultado del cálculo"));

        areaResultado = new JTextArea();
        areaResultado.setEditable(false);
        areaResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areaResultado.setMargin(new java.awt.Insets(8, 8, 8, 8));
        panel.add(new JScrollPane(areaResultado), BorderLayout.CENTER);

        lblEstado = new JLabel(" ");
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD));
        panel.add(lblEstado, BorderLayout.SOUTH);
        return panel;
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    /** Llena la lista con todos los empleados (activos e inactivos, marcados). */
    private void cargarEmpleados() {
        cmbEmpleados.removeAllItems();
        codigosCombo.clear();
        cmbEmpleados.addItem(SELECCIONE);
        codigosCombo.add(null);
        try {
            for (Empleado e : empleadoDAO.listar(null, true)) {
                cmbEmpleados.addItem(e.getCodigo() + " - " + e.getNombreCompleto()
                        + (e.isActivo() ? "" : " (INACTIVO)"));
                codigosCombo.add(e.getCodigo());
            }
        } catch (RuntimeException ex) {
            mostrarError("No se pudo cargar la lista de empleados.\n" + ex.getMessage());
        }
    }

    /** Subproceso 2.1 a 2.6: verifica, calcula y muestra el resultado sin guardarlo. */
    private void calcular() {
        try {
            Calculo c = servicio.calcular(txtCodigo.getText(), txtPeriodo.getText());
            mostrarResultado(c);

            if (servicio.yaRegistrado(c.getCodigoEmpleado(), c.getPeriodo())) {
                btnRegistrar.setEnabled(false);
                lblEstado.setText("Este empleado ya tiene un cálculo registrado en el período "
                        + c.getPeriodo() + ". No se puede registrar otro.");
            } else {
                btnRegistrar.setEnabled(true);
                lblEstado.setText("Cálculo pendiente de registrar. Pulse \"Registrar resultado\" para guardarlo.");
            }
        } catch (CalculoException ex) {
            limpiarResultado();
            mostrarAviso(ex.getMessage());
        } catch (RuntimeException ex) {
            limpiarResultado();
            mostrarError("Error al calcular la remuneración.\n" + ex.getMessage());
        }
    }

    /** Subprocesos 5.1 y 5.2: calcula desde MongoDB y guarda en el historial. */
    private void registrar() {
        try {
            Calculo c = servicio.calcularYRegistrar(txtCodigo.getText(), txtPeriodo.getText());
            mostrarResultado(c);
            btnRegistrar.setEnabled(false);
            lblEstado.setText("Cálculo registrado en el historial.");
            JOptionPane.showMessageDialog(this,
                    "Remuneración de " + c.getNombreEmpleado() + " registrada: " + Calculo.dinero(c.getImporte()),
                    "Cálculo registrado", JOptionPane.INFORMATION_MESSAGE);
        } catch (CalculoException ex) {
            mostrarAviso(ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarError("No se pudo registrar el cálculo.\n" + ex.getMessage());
        }
    }

    private void mostrarResultado(Calculo c) {
        StringBuilder sb = new StringBuilder();
        sb.append("Empleado:      ").append(c.getNombreEmpleado()).append('\n');
        sb.append("Código:        ").append(c.getCodigoEmpleado()).append('\n');
        sb.append("Tipo:          ").append(c.getTipo()).append('\n');
        sb.append("Período:       ").append(c.getPeriodo()).append('\n');
        sb.append('\n');
        sb.append("Datos usados:  ").append(c.getDatosUsados()).append('\n');
        sb.append("Salario base:  ").append(c.getSalarioBaseTexto()).append('\n');
        sb.append("Fórmula:       ").append(c.getFormula()).append('\n');
        sb.append('\n');
        sb.append("REMUNERACIÓN CALCULADA: ").append(Calculo.dinero(c.getImporte())).append('\n');
        areaResultado.setText(sb.toString());
        areaResultado.setCaretPosition(0);
    }

    private void limpiarResultado() {
        if (areaResultado == null) {
            return;
        }
        areaResultado.setText("");
        lblEstado.setText(" ");
        btnRegistrar.setEnabled(false);
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
