
package vista;

import dao.UsuarioDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author G7_GP6
 */

public class LoginFrame extends JFrame {

    // Componentes de la ventana
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnSalir;

    public LoginFrame() {

        // Configuración principal de la ventana
        setTitle("Inicio de Sesión - Sistema de Empleados");
        setSize(420, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la ventana
        setResizable(false);

        // Panel principal donde se colocarán los componentes
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(245, 247, 250));

        // Título del sistema
        JLabel lblTitulo = new JLabel("Sistema de Administración de Empleados");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setBounds(35, 20, 350, 30);
        panel.add(lblTitulo);

        // Etiqueta y campo para el usuario
        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setBounds(70, 80, 80, 25);
        panel.add(lblUsuario);

        txtUsuario = new JTextField();
        txtUsuario.setBounds(150, 80, 200, 28);
        panel.add(txtUsuario);

        // Etiqueta y campo para la contraseña
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setBounds(70, 125, 80, 25);
        panel.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(150, 125, 200, 28);
        panel.add(txtPassword);

        // Botón para ingresar al sistema
        btnIngresar = new JButton("Ingresar");
        btnIngresar.setBounds(150, 180, 100, 32);
        btnIngresar.setBackground(new Color(0, 123, 255));
        btnIngresar.setForeground(Color.BLACK);
        btnIngresar.setFocusPainted(false);
        panel.add(btnIngresar);

        // Botón para cerrar el programa
        btnSalir = new JButton("Salir");
        btnSalir.setBounds(260, 180, 90, 32);
        panel.add(btnSalir);

        // Agregamos el panel a la ventana
        add(panel);

        // Evento del botón Ingresar
        btnIngresar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarSesion();
            }
        });

        // Evento del botón Salir
        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Permite presionar Enter para iniciar sesión
        getRootPane().setDefaultButton(btnIngresar);
    }

    /**
     * Verifica las credenciales ingresadas por el usuario.
     * Si son correctas, permite el acceso al sistema.
     */
    private void iniciarSesion() {

        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        // Verifica que no haya campos vacíos
        if (usuario.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Por favor complete todos los campos.",
                    "Campos vacíos",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // Consultamos en la base de datos
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        boolean esValido = usuarioDAO.validarLogin(usuario, password);

        if (esValido) {

            JOptionPane.showMessageDialog(
                    this,
                    "Bienvenido al sistema.",
                    "Login exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Cerramos el login
            this.dispose();

            // Aquí se abrirá el menú principal
            new MenuPrincipal().setVisible(true);

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Usuario o contraseña incorrectos.",
                    "Error de autenticación",
                    JOptionPane.ERROR_MESSAGE
            );

            // Limpia la contraseña y deja el cursor en ese campo
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    // Método principal para ejecutar y probar la ventana
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}

