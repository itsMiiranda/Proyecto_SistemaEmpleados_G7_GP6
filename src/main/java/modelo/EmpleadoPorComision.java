
package modelo;
import java.time.LocalDate;

/**
 * @author G7_GP6
 * Empleado por comisión.
 * Fórmula: montoVentas × (porcentaje / 100)
 */
public class EmpleadoPorComision extends Empleado {
    
    //Atributos
    private double porcentajeComision; // 0 a 100
    private double montoVentas;
    
    //Métodos
    public EmpleadoPorComision() {
    }
    
    //Método constructor
    public EmpleadoPorComision(String codigo, String nombre, String apellido,
        String documento, String cargo, LocalDate fechaIngreso,
        double porcentajeComision, double montoVentas) {
        super(codigo, nombre, apellido, documento, cargo, fechaIngreso, true, "POR_COMISION");
        this.porcentajeComision = porcentajeComision;
        this.montoVentas = montoVentas;
    }

    //Getters y Setters
    public double getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(double porcentajeComision) {
        this.porcentajeComision = porcentajeComision;
    }

    public double getMontoVentas() {
        return montoVentas;
    }

    public void setMontoVentas(double montoVentas) {
        this.montoVentas = montoVentas;
    }

     // Calcula la remuneración de un empleado por comisión
    @Override
    public double calcularRemuneracion() {
        return montoVentas * (porcentajeComision / 100.0);
    }
    
    
    
}
