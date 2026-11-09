
package modelo;
import java.time.LocalDate;

/**
 *
 * @author G7_GP6
 * Empleado remunerado por horas trabajadas.
 * Fórmula: tarifa × horas
 * 
 */
public class EmpleadoPorHora extends Empleado {
    
    //Atributos propios de la clase
    private double tarifaPorHora;
    private double horasTrabajadas;

    public EmpleadoPorHora() {
    }

    //Constructor de la clase
  public EmpleadoPorHora(String codigo, String nombre, String apellido,
       String documento, String cargo, LocalDate fechaIngreso,
       double tarifaPorHora, double horasTrabajadas) {
        super(codigo, nombre, apellido, documento, cargo, fechaIngreso,true, "POR_HORA");
        this.tarifaPorHora = tarifaPorHora;
        this.horasTrabajadas = horasTrabajadas;
    }
    
    //Getters y Setters

    public double getTarifaPorHora() {
        return tarifaPorHora;
    }

    public void setTarifaPorHora(double tarifaPorHora) {
        this.tarifaPorHora = tarifaPorHora;
    }

    public double getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public void setHorasTrabajadas(double horasTrabajadas) {
        this.horasTrabajadas = horasTrabajadas;
    }

    // Calcula la remuneración de un empleado por hora
    @Override
    public double calcularRemuneracion() {
        return tarifaPorHora * horasTrabajadas;
        
    }


}
