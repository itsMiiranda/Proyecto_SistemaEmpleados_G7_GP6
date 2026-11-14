package vista;

// Importarmos las herramientas para crear vetanas  botones 
import javax.swing.*;
import java.awt.*; 

/**
 *
 * @author G7_GP6
 */

public class MenuPrincipal extends JFrame {

  // Constructor del menu principal 
  public MenuPrincipal() {

    //Configuracion de la ventana 
    setTitle("Sistema de Administracion de Empleados");
    setSize(500, 400);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Titulo que aparecera en la parte superior 
    JLabel titulo = new JLabel("MENÚ PRINCIPAL", SwingConstants.CENTER);
    titulo.setFont(new Font("Arial", Font.BOLD, 22));

    //Creamos los botones del menu
    JButton btnEmpleados = new JButton("Administrar empleados");
    JButton btnRemuneraciones = new JButton("Calcular remuneraciones");
    JButton btnConsultas = new JButton("Consultar empleados"); 
    JButton btnReportes = new JButton("Generar reportes"); 
    JButton btnCerrar = new JButton("Cerrar sesión");

    //Panel para colocar los botones
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(5, 1, 10, 10));

    //Agregamos los botones al panel 
    panel.add(btnEmpleados);
    panel.add(btnRemuneraciones);
    panel.add(btnConsultas);
    panel.add(btnReportes);
    panel.add(btnCerrar);

    //Agregamos el titulo y el panel a la ventana 
    add(titulo, BorderLayout.NORTH);
    add(panel, BorderLayout.CENTER);

    //Accion del boton de empleados
    btnEmpleados.addActionListener(e -> JOptionPane.showMessageDialog(this,"Modulo de empleados"));

    //Accion del botón de remuneraciones
    btnRemuneraciones.addActionListener(e -> JOptionPane.showMessageDialog(this, "Modulo de remuneraciones"));

    // Acción del botón de consultas
        btnConsultas.addActionListener(e -> JOptionPane.showMessageDialog(this, "Módulo de consultas"));

    // Acción del botón de reportes
        btnReportes.addActionListener(e -> JOptionPane.showMessageDialog(this, "Módulo de reportes")
        );

     // Acción para cerrar la sesión
        btnCerrar.addActionListener(e -> {

            // Preguntamos si desea cerrar la sesión
            int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Desea cerrar sesión?",
                "Cerrar sesión",
                JOptionPane.YES_NO_OPTION
            );

            // Si acepta, regresamos al inicio de sesión
            if (opcion == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
  }
}
