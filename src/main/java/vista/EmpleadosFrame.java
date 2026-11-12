package vista;

import dao.EmpleadoDAO;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.Empleado;
import modelo.EmpleadoAdministrativo;
import modelo.EmpleadoPorComision;
import modelo.EmpleadoPorHora;
import util.ValidadorEmpleado;

/**
 * Pantalla de gestión de empleados: formulario de registro/modificación,
 * tabla de empleados, búsqueda por código, documento o nombre y filtro por tipo.
 *
 * @author G7_GP6
 */
public class EmpleadosFrame extends JFrame {

    private static final String TODOS = "TODOS";
    private static final String[] TIPOS = {
        EmpleadoDAO.ADMINISTRATIVO, EmpleadoDAO.POR_HORA, EmpleadoDAO.POR_COMISION
    };
    private static final String[] COLUMNAS = {
        "Código", "Nombre", "Documento", "Cargo", "Ingreso", "Tipo", "Estado", "Remuneración"
    };

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    // Formulario
    private JTextField txtCodigo;
    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JTextField txtDocumento;
    private JTextField txtCargo;
    private JTextField txtFechaIngreso;
    private JComboBox<String> cmbTipo;
    private JPanel panelTipo;
    private CardLayout cardTipo;
    private JTextField txtSalarioBase;
    private JTextField txtTarifaHora;
    private JTextField txtHoras;
    private JTextField txtPorcentaje;
    private JTextField txtVentas;

    // Botones del formulario
    private JButton btnGuardar;
    private JButton btnModificar;
    private JButton btnInactivar;
    private JButton btnLimpiar;

    // Búsqueda y filtro
    private JTextField txtBuscar;
    private JComboBox<String> cmbFiltroTipo;
    private JCheckBox chkInactivos;

    // Tabla
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JLabel lblResultados;

    // Empleados que muestra la tabla (misma posición que las filas)
    private List<Empleado> empleadosMostrados;

    public EmpleadosFrame() {
        setTitle("Gestión de Empleados");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        raiz.add(crearPanelFormulario(), BorderLayout.NORTH);
        raiz.add(crearPanelTabla(), BorderLayout.CENTER);
        setContentPane(raiz);

        modoNuevo();
        cargarTabla();
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del empleado"));

        txtCodigo = new JTextField(12);
        txtNombres = new JTextField(16);
        txtApellidos = new JTextField(16);
        txtDocumento = new JTextField(12);
        txtCargo = new JTextField(16);
        txtFechaIngreso = new JTextField(10);
        txtFechaIngreso.setToolTipText("Formato aaaa-mm-dd");
        cmbTipo = new JComboBox<>(TIPOS);
        cmbTipo.setSelectedIndex(-1);

        // Fila 0
        agregar(panel, new JLabel("Código:"), 0, 0);
        agregar(panel, txtCodigo, 1, 0);
        agregar(panel, new JLabel("Nombres:"), 2, 0);
        agregar(panel, txtNombres, 3, 0);
        agregar(panel, new JLabel("Apellidos:"), 4, 0);
        agregar(panel, txtApellidos, 5, 0);

        // Fila 1
        agregar(panel, new JLabel("Documento:"), 0, 1);
        agregar(panel, txtDocumento, 1, 1);
        agregar(panel, new JLabel("Cargo:"), 2, 1);
        agregar(panel, txtCargo, 3, 1);
        agregar(panel, new JLabel("Ingreso (aaaa-mm-dd):"), 4, 1);
        agregar(panel, txtFechaIngreso, 5, 1);

        // Fila 2: tipo y campos propios del tipo
        agregar(panel, new JLabel("Tipo:"), 0, 2);
        agregar(panel, cmbTipo, 1, 2);

        cardTipo = new CardLayout();
        panelTipo = new JPanel(cardTipo);
        panelTipo.add(new JPanel(), "VACIO");

        txtSalarioBase = new JTextField(10);
        JPanel pAdmin = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pAdmin.add(new JLabel("Salario base mensual:"));
        pAdmin.add(txtSalarioBase);
        panelTipo.add(pAdmin, EmpleadoDAO.ADMINISTRATIVO);

        txtTarifaHora = new JTextField(8);
        txtHoras = new JTextField(8);
        JPanel pHora = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pHora.add(new JLabel("Tarifa por hora:"));
        pHora.add(txtTarifaHora);
        pHora.add(new JLabel("Horas trabajadas:"));
        pHora.add(txtHoras);
        panelTipo.add(pHora, EmpleadoDAO.POR_HORA);

        txtPorcentaje = new JTextField(6);
        txtVentas = new JTextField(10);
        JPanel pComision = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pComision.add(new JLabel("Comisión (%):"));
        pComision.add(txtPorcentaje);
        pComision.add(new JLabel("Monto de ventas:"));
        pComision.add(txtVentas);
        panelTipo.add(pComision, EmpleadoDAO.POR_COMISION);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 2;
        gc.gridy = 2;
        gc.gridwidth = 4;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 6, 4, 6);
        panel.add(panelTipo, gc);

        cmbTipo.addActionListener(e -> mostrarCamposDelTipo());

        // Fila 3: botones
        btnGuardar = new JButton("Guardar nuevo");
        btnModificar = new JButton("Modificar");
        btnInactivar = new JButton("Inactivar");
        btnLimpiar = new JButton("Limpiar / Nuevo");

        btnGuardar.addActionListener(e -> guardarNuevo());
        btnModificar.addActionListener(e -> modificarSeleccionado());
        btnInactivar.addActionListener(e -> inactivarSeleccionado());
        btnLimpiar.addActionListener(e -> {
            tabla.clearSelection();
            modoNuevo();
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.add(btnGuardar);
        botones.add(btnModificar);
        botones.add(btnInactivar);
        botones.add(btnLimpiar);

        gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 3;
        gc.gridwidth = 6;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(8, 6, 6, 6);
        panel.add(botones, gc);

        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));

        // Barra de búsqueda y filtro
        txtBuscar = new JTextField(18);
        txtBuscar.setToolTipText("Código, documento o nombre");
        cmbFiltroTipo = new JComboBox<>(new String[]{TODOS, EmpleadoDAO.ADMINISTRATIVO,
            EmpleadoDAO.POR_HORA, EmpleadoDAO.POR_COMISION});
        chkInactivos = new JCheckBox("Mostrar inactivos", true);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnTodos = new JButton("Mostrar todos");

        btnBuscar.addActionListener(e -> cargarTabla());
        txtBuscar.addActionListener(e -> cargarTabla());
        cmbFiltroTipo.addActionListener(e -> cargarTabla());
        chkInactivos.addActionListener(e -> cargarTabla());
        btnTodos.addActionListener(e -> {
            txtBuscar.setText("");
            cmbFiltroTipo.setSelectedItem(TODOS);
            cargarTabla();
        });

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setBorder(BorderFactory.createTitledBorder("Búsqueda y filtro"));
        barra.add(new JLabel("Buscar:"));
        barra.add(txtBuscar);
        barra.add(btnBuscar);
        barra.add(new JLabel("Tipo:"));
        barra.add(cmbFiltroTipo);
        barra.add(chkInactivos);
        barra.add(btnTodos);
        panel.add(barra, BorderLayout.NORTH);

        // Tabla
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.setAutoCreateRowSorter(true); // permite ordenar por nombre, código o tipo
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD));

        // Los empleados inactivos se muestran en gris
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                    boolean foco, int fila, int columna) {
                Component c = super.getTableCellRendererComponent(t, valor, sel, foco, fila, columna);
                int filaModelo = t.convertRowIndexToModel(fila);
                boolean inactivo = "Inactivo".equals(modeloTabla.getValueAt(filaModelo, 6));
                if (!sel) {
                    c.setForeground(inactivo ? Color.GRAY : t.getForeground());
                }
                return c;
            }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        lblResultados = new JLabel(" ");
        panel.add(lblResultados, BorderLayout.SOUTH);
        return panel;
    }

    private void agregar(JPanel panel, Component comp, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(4, 6, 4, 6);
        panel.add(comp, gc);
    }

    // ------------------------------------------------------------------
    // Carga de datos
    // ------------------------------------------------------------------

    /** Consulta MongoDB según la búsqueda y el filtro actuales y llena la tabla. */
    private void cargarTabla() {
        String tipo = TODOS.equals(cmbFiltroTipo.getSelectedItem())
                ? null : (String) cmbFiltroTipo.getSelectedItem();
        String texto = txtBuscar.getText().trim();

        try {
            empleadosMostrados = empleadoDAO.buscar(texto, tipo, chkInactivos.isSelected());
        } catch (RuntimeException ex) {
            mostrarError("No se pudieron consultar los empleados.\n" + ex.getMessage());
            return;
        }

        modeloTabla.setRowCount(0);
        for (Empleado e : empleadosMostrados) {
            modeloTabla.addRow(new Object[]{
                e.getCodigo(),
                e.getNombreCompleto(),
                e.getDocumento(),
                e.getCargo(),
                e.getFechaIngreso() == null ? "" : e.getFechaIngreso().format(DateTimeFormatter.ISO_LOCAL_DATE),
                e.getTipo(),
                e.isActivo() ? "Activo" : "Inactivo",
                String.format(Locale.US, "$%,.2f", e.calcularRemuneracion())
            });
        }

        if (empleadosMostrados.isEmpty()) {
            lblResultados.setText("No se encontraron empleados con los criterios indicados.");
        } else {
            lblResultados.setText(empleadosMostrados.size() + " empleado(s) encontrado(s).");
        }
        modoNuevo();
    }

    /** Copia el empleado seleccionado de la tabla al formulario (modo edición). */
    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        Empleado e = empleadosMostrados.get(tabla.convertRowIndexToModel(fila));

        txtCodigo.setText(e.getCodigo());
        txtNombres.setText(e.getNombre());
        txtApellidos.setText(e.getApellido());
        txtDocumento.setText(e.getDocumento());
        txtCargo.setText(e.getCargo());
        txtFechaIngreso.setText(e.getFechaIngreso() == null ? "" : e.getFechaIngreso().toString());
        cmbTipo.setSelectedItem(e.getTipo());

        if (e instanceof EmpleadoAdministrativo) {
            txtSalarioBase.setText(formatear(((EmpleadoAdministrativo) e).getSalarioBase()));
        } else if (e instanceof EmpleadoPorHora) {
            txtTarifaHora.setText(formatear(((EmpleadoPorHora) e).getTarifaPorHora()));
            txtHoras.setText(formatear(((EmpleadoPorHora) e).getHorasTrabajadas()));
        } else if (e instanceof EmpleadoPorComision) {
            txtPorcentaje.setText(formatear(((EmpleadoPorComision) e).getPorcentajeComision()));
            txtVentas.setText(formatear(((EmpleadoPorComision) e).getMontoVentas()));
        }

        // Modo edición: el código y el tipo no se pueden cambiar
        txtCodigo.setEnabled(false);
        cmbTipo.setEnabled(false);
        btnGuardar.setEnabled(false);
        btnModificar.setEnabled(e.isActivo());
        btnInactivar.setEnabled(e.isActivo());
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    private void guardarNuevo() {
        Empleado nuevo = leerFormulario(true);
        if (nuevo == null) {
            return; // ya se mostró el mensaje de validación
        }
        try {
            empleadoDAO.insertar(nuevo);
        } catch (IllegalArgumentException ex) {
            mostrarAviso(ex.getMessage());
            txtCodigo.requestFocus();
            return;
        } catch (RuntimeException ex) {
            mostrarError("No se pudo guardar el empleado.\n" + ex.getMessage());
            return;
        }
        JOptionPane.showMessageDialog(this, "Empleado registrado correctamente.",
                "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
        cargarTabla();
    }

    private void modificarSeleccionado() {
        if (tabla.getSelectedRow() < 0) {
            mostrarAviso("Seleccione un empleado de la tabla para modificarlo.");
            return;
        }
        Empleado datos = leerFormulario(false);
        if (datos == null) {
            return;
        }
        try {
            if (!empleadoDAO.modificar(datos)) {
                mostrarAviso("El empleado " + datos.getCodigo() + " ya no existe en la base de datos.");
                cargarTabla();
                return;
            }
        } catch (RuntimeException ex) {
            mostrarError("No se pudo modificar el empleado.\n" + ex.getMessage());
            return;
        }
        JOptionPane.showMessageDialog(this, "Empleado modificado correctamente.",
                "Modificación exitosa", JOptionPane.INFORMATION_MESSAGE);
        cargarTabla();
    }

    private void inactivarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarAviso("Seleccione un empleado de la tabla para inactivarlo.");
            return;
        }
        Empleado e = empleadosMostrados.get(tabla.convertRowIndexToModel(fila));
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Desea inactivar a " + e.getNombreCompleto() + " (" + e.getCodigo() + ")?\n"
                + "No participará en nuevos cálculos, pero su información se conservará.",
                "Confirmar inactivación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (!empleadoDAO.inactivar(e.getCodigo())) {
                mostrarAviso("El empleado " + e.getCodigo() + " ya no existe en la base de datos.");
                cargarTabla();
                return;
            }
        } catch (RuntimeException ex) {
            mostrarError("No se pudo inactivar el empleado.\n" + ex.getMessage());
            return;
        }
        JOptionPane.showMessageDialog(this, "Empleado inactivado correctamente.",
                "Inactivación exitosa", JOptionPane.INFORMATION_MESSAGE);
        cargarTabla();
    }

    // ------------------------------------------------------------------
    // Validación y lectura del formulario
    // ------------------------------------------------------------------

    /**
     * Valida el formulario y construye el empleado según el tipo seleccionado.
     * Devuelve null (tras mostrar el mensaje) si algún dato es inválido.
     *
     * @param esNuevo si es true se valida también el código
     */
    private Empleado leerFormulario(boolean esNuevo) {
        String[] errores = {
            esNuevo ? ValidadorEmpleado.obligatorio(txtCodigo.getText(), "Código") : null,
            ValidadorEmpleado.obligatorio(txtNombres.getText(), "Nombres"),
            ValidadorEmpleado.obligatorio(txtApellidos.getText(), "Apellidos"),
            ValidadorEmpleado.documento(txtDocumento.getText()),
            ValidadorEmpleado.obligatorio(txtCargo.getText(), "Cargo"),
            ValidadorEmpleado.fechaIngreso(txtFechaIngreso.getText()),
            ValidadorEmpleado.tipo(cmbTipo.getSelectedItem())
        };
        for (String error : errores) {
            if (error != null) {
                mostrarAviso(error);
                return null;
            }
        }

        String tipo = (String) cmbTipo.getSelectedItem();
        String error = null;
        switch (tipo) {
            case EmpleadoDAO.ADMINISTRATIVO:
                error = ValidadorEmpleado.numeroNoNegativo(txtSalarioBase.getText(), "Salario base");
                break;
            case EmpleadoDAO.POR_HORA:
                error = ValidadorEmpleado.numeroNoNegativo(txtTarifaHora.getText(), "Tarifa por hora");
                if (error == null) {
                    error = ValidadorEmpleado.numeroNoNegativo(txtHoras.getText(), "Horas trabajadas");
                }
                break;
            case EmpleadoDAO.POR_COMISION:
                error = ValidadorEmpleado.porcentaje(txtPorcentaje.getText());
                if (error == null) {
                    error = ValidadorEmpleado.numeroNoNegativo(txtVentas.getText(), "Monto de ventas");
                }
                break;
            default:
                break;
        }
        if (error != null) {
            mostrarAviso(error);
            return null;
        }

        String codigo = txtCodigo.getText().trim();
        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String documento = txtDocumento.getText().trim();
        String cargo = txtCargo.getText().trim();
        LocalDate fecha = LocalDate.parse(txtFechaIngreso.getText().trim());

        Empleado e;
        if (EmpleadoDAO.ADMINISTRATIVO.equals(tipo)) {
            e = new EmpleadoAdministrativo(codigo, nombres, apellidos, documento, cargo, fecha,
                    ValidadorEmpleado.aNumero(txtSalarioBase.getText()));
        } else if (EmpleadoDAO.POR_HORA.equals(tipo)) {
            e = new EmpleadoPorHora(codigo, nombres, apellidos, documento, cargo, fecha,
                    ValidadorEmpleado.aNumero(txtTarifaHora.getText()),
                    ValidadorEmpleado.aNumero(txtHoras.getText()));
        } else {
            e = new EmpleadoPorComision(codigo, nombres, apellidos, documento, cargo, fecha,
                    ValidadorEmpleado.aNumero(txtPorcentaje.getText()),
                    ValidadorEmpleado.aNumero(txtVentas.getText()));
        }
        return e;
    }

    // ------------------------------------------------------------------
    // Estado del formulario
    // ------------------------------------------------------------------

    /** Deja el formulario vacío y listo para registrar un empleado nuevo. */
    private void modoNuevo() {
        txtCodigo.setText("");
        txtNombres.setText("");
        txtApellidos.setText("");
        txtDocumento.setText("");
        txtCargo.setText("");
        txtFechaIngreso.setText("");
        txtSalarioBase.setText("");
        txtTarifaHora.setText("");
        txtHoras.setText("");
        txtPorcentaje.setText("");
        txtVentas.setText("");
        cmbTipo.setSelectedIndex(-1);

        txtCodigo.setEnabled(true);
        cmbTipo.setEnabled(true);
        btnGuardar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnInactivar.setEnabled(false);
    }

    private void mostrarCamposDelTipo() {
        Object tipo = cmbTipo.getSelectedItem();
        cardTipo.show(panelTipo, tipo == null ? "VACIO" : (String) tipo);
    }

    private String formatear(double valor) {
        // Muestra 160 en lugar de 160.0 cuando el número es entero
        return valor == Math.rint(valor) ? String.valueOf((long) valor) : String.valueOf(valor);
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Método principal para probar la pantalla de forma independiente
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new EmpleadosFrame().setVisible(true));
    }
}
