
package modelo;
import java.time.LocalDate;

/**
 * @author G7_GP6
 * Empleado con salario mensual fijo
 */
public class EmpleadoAdministrativo extends Empleado {
    //Atributo propio de la clase
    private double salarioBase;
    
    public EmpleadoAdministrativo() {
    }

    //Método constructor
   public EmpleadoAdministrativo(String codigo, String nombre, String apellido,String documento, String cargo, LocalDate fechaIngreso,
   double salarioBase) {
       super(codigo, nombre, apellido, documento, cargo, fechaIngreso, true, "ADMINISTRATIVO");
       this.salarioBase = salarioBase;
    }
   
   //Getter y Setter

    public double getSalarioBase() {
        return salarioBase;
    }

    public void setSalarioBase(double salarioBase) {
        this.salarioBase = salarioBase;
    }
   
    // Calcula la remuneración de un empleado administrativo
    
    @Override
    public double calcularRemuneracion() {
        return salarioBase;
    }
    
    
}
