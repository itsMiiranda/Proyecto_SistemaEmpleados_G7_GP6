
package modelo;

import org.bson.types.ObjectId;
import java.time.LocalDate;

/**
 * @author G7_GP6
 * Clase abstracta base para todos los tipos de empleados.
 * Contiene los datos comunes y define el método abstracto
 * para calcular la remuneración (polimorfismo).
 */
public abstract class Empleado {
    
    //Atributos 
    private String codigo;    // Código único de negocio
    private String nombre;
    private String apellido;
    private String documento; // DUI u otro documento
    private String cargo;
    private LocalDate fechaIngreso;
    private boolean activo;
    private String tipo;     // ADMINISTRATIVO, POR_HORA, POR_COMISION

    public Empleado() {
        this.activo = true;
    }

    //Creamos el constructor con sus respectivos parámetros
    public Empleado(String codigo, String nombre, String apellido, String documento, String cargo, LocalDate fechaIngreso, boolean activo, String tipo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        this.cargo = cargo;
        this.fechaIngreso = fechaIngreso;
        this.activo = activo;
        this.tipo = tipo;
    }
    
    //Getters y setters

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    // Nombre completo (útil para mostrar en tablas)
    public String getNombreCompleto() {
        return ((nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido)).trim();
    }  
    
    // Método abstracto → cada hijo lo implementa de forma distinta
    public abstract double calcularRemuneracion();
    
    
}
