package modelo;

import dao.EmpleadoDAO;
import java.time.LocalDateTime;
import java.util.Locale;


public class Calculo {

    private String codigoEmpleado;
    private String nombreEmpleado;
    private String tipo;
    private String periodo; 

    // Valores usados
    private Double salarioBase;
    private Double tarifaHora;
    private Double horasTrabajadas;
    private Double montoVentas;
    private Double porcentajeComision;

    private double importe;
    private LocalDateTime fechaCalculo;

    public Calculo() {
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(String codigoEmpleado) {
        this.codigoEmpleado = codigoEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Double getSalarioBase() {
        return salarioBase;
    }

    public void setSalarioBase(Double salarioBase) {
        this.salarioBase = salarioBase;
    }

    public Double getTarifaHora() {
        return tarifaHora;
    }

    public void setTarifaHora(Double tarifaHora) {
        this.tarifaHora = tarifaHora;
    }

    public Double getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public void setHorasTrabajadas(Double horasTrabajadas) {
        this.horasTrabajadas = horasTrabajadas;
    }

    public Double getMontoVentas() {
        return montoVentas;
    }

    public void setMontoVentas(Double montoVentas) {
        this.montoVentas = montoVentas;
    }

    public Double getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(Double porcentajeComision) {
        this.porcentajeComision = porcentajeComision;
    }

    public double getImporte() {
        return importe;
    }

    public void setImporte(double importe) {
        this.importe = importe;
    }

    public LocalDateTime getFechaCalculo() {
        return fechaCalculo;
    }

    public void setFechaCalculo(LocalDateTime fechaCalculo) {
        this.fechaCalculo = fechaCalculo;
    }

    /** Salario base como texto para reportes: solo aplica a empleados administrativos. */
    public String getSalarioBaseTexto() {
        return salarioBase == null ? "-" : dinero(salarioBase);
    }

    /** Describe los valores que se usaron en el cálculo, según el tipo de empleado. */
    public String getDatosUsados() {
        if (EmpleadoDAO.ADMINISTRATIVO.equals(tipo)) {
            return "Salario base mensual: " + dinero(valor(salarioBase));
        }
        if (EmpleadoDAO.POR_HORA.equals(tipo)) {
            return String.format(Locale.US, "Tarifa por hora: %s | Horas trabajadas: %.2f",
                    dinero(valor(tarifaHora)), valor(horasTrabajadas));
        }
        if (EmpleadoDAO.POR_COMISION.equals(tipo)) {
            return String.format(Locale.US, "Monto de ventas: %s | Comisión: %.2f%%",
                    dinero(valor(montoVentas)), valor(porcentajeComision));
        }
        return "";
    }

    /** Fórmula aplicada según el tipo de empleado. */
    public String getFormula() {
        if (EmpleadoDAO.ADMINISTRATIVO.equals(tipo)) {
            return "Remuneración = salario base mensual";
        }
        if (EmpleadoDAO.POR_HORA.equals(tipo)) {
            return "Remuneración = tarifa por hora × horas trabajadas";
        }
        if (EmpleadoDAO.POR_COMISION.equals(tipo)) {
            return "Remuneración = monto de ventas × porcentaje de comisión / 100";
        }
        return "";
    }

    /** Formatea un monto  */
    public static String dinero(double monto) {
        return String.format(Locale.US, "$%,.2f", monto);
    }

    private static double valor(Double d) {
        return d == null ? 0.0 : d;
    }
}
