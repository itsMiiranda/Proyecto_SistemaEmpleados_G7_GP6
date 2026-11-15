package vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Menú principal: se abre después de un inicio de sesión correcto y enlaza
 * las pantallas del sistema.
 *
 * @author G7_GP6
 */
public class MenuPrincipal extends JFrame {

    public MenuPrincipal() {
        setTitle("Menú principal - Sistema de Empleados");
        setSize(440, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel raiz = new JPanel(new java.awt.BorderLayout(0, 15));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        raiz.setBackground(new Color(245, 247, 250));

        JLabel titulo = new JLabel("Sistema de Administración de Empleados", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        raiz.add(titulo, java.awt.BorderLayout.NORTH);

        JPanel botones = new JPanel(new GridLayout(5, 1, 0, 10));
        botones.setOpaque(false);

        JButton btnEmpleados = crearBoton("Gestión de empleados");
        JButton btnCalculos = crearBoton("Cálculo de remuneraciones");
        JButton btnReportes = crearBoton("Reportes e historial");
        JButton btnSesion = crearBoton("Cerrar sesión");
        JButton btnSalir = crearBoton("Salir");

        btnEmpleados.addActionListener(e -> abrir("empleados"));
        btnCalculos.addActionListener(e -> abrir("calculos"));
        btnReportes.addActionListener(e -> abrir("reportes"));
        btnSesion.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        btnSalir.addActionListener(e -> System.exit(0));

        botones.add(btnEmpleados);
        botones.add(btnCalculos);
        botones.add(btnReportes);
        botones.add(btnSesion);
        botones.add(btnSalir);
        raiz.add(botones, java.awt.BorderLayout.CENTER);

        setContentPane(raiz);
    }

    private JButton crearBoton(String texto) {
        JButton b = new JButton(texto);
        b.setPreferredSize(new Dimension(300, 36));
        return b;
    }

    /** Abre una pantalla; si MongoDB no responde muestra el error en vez de cerrarse. */
    private void abrir(String pantalla) {
        try {
            JFrame ventana;
            switch (pantalla) {
                case "empleados":
                    ventana = new EmpleadosFrame();
                    break;
                case "calculos":
                    ventana = new CalculosFrame();
                    break;
                default:
                    ventana = new ReportesFrame();
                    break;
            }
            ventana.setVisible(true);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir la pantalla.\n" + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
}
