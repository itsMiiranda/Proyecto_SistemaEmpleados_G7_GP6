package servicio;

import dao.CalculoDAO;
import dao.EmpleadoDAO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import modelo.Calculo;
import modelo.Empleado;
import modelo.EmpleadoAdministrativo;
import modelo.EmpleadoPorComision;
import modelo.EmpleadoPorHora;
import util.ValidadorEmpleado;

/**
 * Lógica del Proceso 2 (cálculo de remuneraciones) y del registro en el
 * historial (Proceso 5).
 *
 * Reglas:
 *  - El empleado debe existir, estar activo y tener datos válidos.
 *  - El importe SIEMPRE se calcula con calcularRemuneracion() a partir de los
 *    datos guardados en MongoDB; nunca se acepta un importe escrito a mano.
 *  - Un empleado solo puede tener un cálculo registrado por período (aaaa-mm).
 *  - Los cálculos registrados no se modifican.
 *
 * @author G7_GP6
 */
public class CalculoService {

    private static final Pattern PERIODO = Pattern.compile("\\d{4}-(0[1-9]|1[0-2])");

    private final EmpleadoDAO empleadoDAO;
    private final CalculoDAO calculoDAO;

    public CalculoService() {
        this(new EmpleadoDAO(), new CalculoDAO());
    }

    public CalculoService(EmpleadoDAO empleadoDAO, CalculoDAO calculoDAO) {
        this.empleadoDAO = empleadoDAO;
        this.calculoDAO = calculoDAO;
    }

    // ------------------------------------------------------------------
    // Validaciones (no necesitan base de datos)
    // ------------------------------------------------------------------

    /** Valida el período con formato aaaa-mm. Devuelve el error o null si es válido. */
    public static String validarPeriodo(String periodo) {
        String error = ValidadorEmpleado.obligatorio(periodo, "Período");
        if (error != null) {
            return error;
        }
        if (!PERIODO.matcher(periodo.trim()).matches()) {
            return "El período debe tener el formato aaaa-mm (ejemplo: 2026-11).";
        }
        return null;
    }

    /**
     * Verifica que el empleado pueda participar en un cálculo (subproceso 2.2).
     * Devuelve el mensaje de error, o null si todo está correcto.
     */
    public static String verificar(Empleado e) {
        if (e == null) {
            return "El empleado no existe.";
        }
        if (!e.isActivo()) {
            return "El empleado " + e.getCodigo() + " está inactivo; no se puede calcular su remuneración.";
        }
        if (e.getCodigo() == null || e.getCodigo().trim().isEmpty()
                || e.getNombreCompleto().isEmpty()
                || e.getTipo() == null || e.getTipo().trim().isEmpty()) {
            return "El empleado tiene datos incompletos (código, nombre o tipo); no se puede calcular.";
        }

        String error = null;
        if (e instanceof EmpleadoAdministrativo) {
            error = ValidadorEmpleado.numeroNoNegativo(
                    String.valueOf(((EmpleadoAdministrativo) e).getSalarioBase()), "Salario base");
        } else if (e instanceof EmpleadoPorHora) {
            EmpleadoPorHora h = (EmpleadoPorHora) e;
            error = ValidadorEmpleado.numeroNoNegativo(String.valueOf(h.getTarifaPorHora()), "Tarifa por hora");
            if (error == null) {
                error = ValidadorEmpleado.numeroNoNegativo(String.valueOf(h.getHorasTrabajadas()), "Horas trabajadas");
            }
        } else if (e instanceof EmpleadoPorComision) {
            EmpleadoPorComision c = (EmpleadoPorComision) e;
            error = ValidadorEmpleado.numeroNoNegativo(String.valueOf(c.getMontoVentas()), "Monto de ventas");
            if (error == null) {
                error = ValidadorEmpleado.porcentaje(String.valueOf(c.getPorcentajeComision()));
            }
        } else {
            error = "El tipo de empleado no es válido para el cálculo.";
        }
        return error == null ? null : "Datos incompletos o inválidos del empleado " + e.getCodigo() + ": " + error;
    }

    /**
     * Arma el cálculo de un empleado (subprocesos 2.3, 2.4 y 2.5). La fórmula
     * de cada tipo vive en calcularRemuneracion() de su clase (polimorfismo).
     * No consulta ni escribe en la base de datos.
     */
    public static Calculo construir(Empleado e, String periodo) {
        Calculo c = new Calculo();
        c.setCodigoEmpleado(e.getCodigo());
        c.setNombreEmpleado(e.getNombreCompleto());
        c.setTipo(e.getTipo());
        c.setPeriodo(periodo);

        if (e instanceof EmpleadoAdministrativo) {
            c.setSalarioBase(((EmpleadoAdministrativo) e).getSalarioBase());
        } else if (e instanceof EmpleadoPorHora) {
            c.setTarifaHora(((EmpleadoPorHora) e).getTarifaPorHora());
            c.setHorasTrabajadas(((EmpleadoPorHora) e).getHorasTrabajadas());
        } else if (e instanceof EmpleadoPorComision) {
            c.setMontoVentas(((EmpleadoPorComision) e).getMontoVentas());
            c.setPorcentajeComision(((EmpleadoPorComision) e).getPorcentajeComision());
        }

        c.setImporte(redondear(e.calcularRemuneracion()));
        c.setFechaCalculo(LocalDateTime.now());
        return c;
    }

    /** Redondea a 2 decimales (centavos) para evitar errores de punto flotante. */
    public static double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // ------------------------------------------------------------------
    // Operaciones con base de datos
    // ------------------------------------------------------------------

    /**
     * Calcula la remuneración de un empleado SIN guardarla (vista previa).
     *
     * @throws CalculoException si el período es inválido, el empleado no existe,
     *                          está inactivo o tiene datos inválidos
     */
    public Calculo calcular(String codigoEmpleado, String periodo) {
        String errorCodigo = ValidadorEmpleado.obligatorio(codigoEmpleado, "Código del empleado");
        if (errorCodigo != null) {
            throw new CalculoException(errorCodigo);
        }
        String errorPeriodo = validarPeriodo(periodo);
        if (errorPeriodo != null) {
            throw new CalculoException(errorPeriodo);
        }

        Empleado e = empleadoDAO.buscarPorCodigo(codigoEmpleado.trim());
        if (e == null) {
            throw new CalculoException("No existe un empleado con el código " + codigoEmpleado.trim() + ".");
        }
        String error = verificar(e);
        if (error != null) {
            throw new CalculoException(error);
        }
        return construir(e, periodo.trim());
    }

    /** Indica si el empleado ya tiene un cálculo registrado en ese período. */
    public boolean yaRegistrado(String codigoEmpleado, String periodo) {
        return calculoDAO.existe(codigoEmpleado.trim(), periodo.trim());
    }

    /**
     * Calcula y registra el resultado en el historial (subprocesos 5.1 y 5.2).
     * Vuelve a leer al empleado desde MongoDB para que el importe salga de los
     * datos registrados.
     *
     * @throws CalculoException si no se cumple alguna regla (incluye período repetido)
     * @throws RuntimeException si falla el cálculo o la escritura en MongoDB
     */
    public Calculo calcularYRegistrar(String codigoEmpleado, String periodo) {
        Calculo c = calcular(codigoEmpleado, periodo);
        if (calculoDAO.existe(c.getCodigoEmpleado(), c.getPeriodo())) {
            throw new CalculoException("Ya existe un cálculo registrado para el empleado "
                    + c.getCodigoEmpleado() + " en el período " + c.getPeriodo()
                    + ". El historial no se puede modificar.");
        }
        try {
            calculoDAO.insertar(c);
        } catch (IllegalArgumentException e) {
            throw new CalculoException(e.getMessage());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al registrar la remuneración: " + e.getMessage(), e);
        }
        return c;
    }
}
