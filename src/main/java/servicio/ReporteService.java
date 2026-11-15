package servicio;

import dao.CalculoDAO;
import dao.EmpleadoDAO;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import modelo.Calculo;
import modelo.Empleado;

/**
 * Proceso 4: generación de reportes.
 *
 * Los reportes distinguen entre el salario base (solo empleados administrativos),
 * los datos usados y la remuneración calculada. Las remuneraciones se calculan
 * al momento a partir de los datos registrados de los empleados ACTIVOS; el
 * historial se lee tal como quedó guardado.
 *
 * @author G7_GP6
 */
public class ReporteService {

    private static final String[] TIPOS = {
        EmpleadoDAO.ADMINISTRATIVO, EmpleadoDAO.POR_HORA, EmpleadoDAO.POR_COMISION
    };
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EmpleadoDAO empleadoDAO;
    private final CalculoDAO calculoDAO;

    public ReporteService() {
        this(new EmpleadoDAO(), new CalculoDAO());
    }

    public ReporteService(EmpleadoDAO empleadoDAO, CalculoDAO calculoDAO) {
        this.empleadoDAO = empleadoDAO;
        this.calculoDAO = calculoDAO;
    }

    // ------------------------------------------------------------------
    // 4.1 Listado general
    // ------------------------------------------------------------------

    public TablaReporte listadoGeneral(boolean incluirInactivos) {
        List<Empleado> lista = empleadoDAO.listar(null, incluirInactivos);
        return tablaListado("Listado general de empleados", lista);
    }

    // ------------------------------------------------------------------
    // 4.2 Listado por tipo
    // ------------------------------------------------------------------

    /** @param tipo ADMINISTRATIVO, POR_HORA o POR_COMISION; null = todos agrupados por tipo */
    public TablaReporte listadoPorTipo(String tipo, boolean incluirInactivos) {
        validarTipo(tipo);
        List<Empleado> lista = empleadoDAO.listar(tipo, incluirInactivos);
        if (tipo == null) {
            lista.sort(Comparator.comparing(Empleado::getTipo).thenComparing(Empleado::getCodigo));
        }
        String titulo = tipo == null ? "Listado de empleados por tipo" : "Listado de empleados: " + tipo;
        return tablaListado(titulo, lista);
    }

    private TablaReporte tablaListado(String titulo, List<Empleado> lista) {
        String[] columnas = {"Código", "Nombre", "Documento", "Cargo", "Fecha de ingreso", "Tipo", "Estado"};
        List<Object[]> filas = new ArrayList<>();
        for (Empleado e : lista) {
            filas.add(new Object[]{
                e.getCodigo(),
                e.getNombreCompleto(),
                e.getDocumento(),
                e.getCargo(),
                e.getFechaIngreso() == null ? "" : e.getFechaIngreso().toString(),
                e.getTipo(),
                e.isActivo() ? "Activo" : "Inactivo"
            });
        }

        String pie;
        if (lista.isEmpty()) {
            pie = "No se encontraron empleados con los criterios indicados.";
        } else {
            StringBuilder sb = new StringBuilder(lista.size() + " empleado(s)");
            for (String t : TIPOS) {
                long n = lista.stream().filter(e -> t.equals(e.getTipo())).count();
                if (n > 0) {
                    sb.append(" | ").append(t).append(": ").append(n);
                }
            }
            pie = sb.toString();
        }
        return new TablaReporte(titulo, columnas, filas, pie);
    }

    // ------------------------------------------------------------------
    // 4.3 Remuneraciones individuales
    // ------------------------------------------------------------------

    /**
     * Calcula la remuneración de cada empleado activo con sus datos actuales.
     * Los empleados con datos inválidos se omiten y se avisa en el pie.
     *
     * @param tipo null = todos los tipos
     */
    public TablaReporte remuneracionesIndividuales(String tipo) {
        validarTipo(tipo);
        List<Empleado> lista = empleadoDAO.listar(tipo, false);

        String[] columnas = {"Código", "Nombre", "Tipo", "Salario base", "Datos usados", "Remuneración calculada"};
        List<Object[]> filas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int omitidos = 0;

        for (Empleado e : lista) {
            if (CalculoService.verificar(e) != null) {
                omitidos++;
                continue;
            }
            Calculo c = CalculoService.construir(e, null);
            total = total.add(BigDecimal.valueOf(c.getImporte()));
            filas.add(new Object[]{
                c.getCodigoEmpleado(),
                c.getNombreEmpleado(),
                c.getTipo(),
                c.getSalarioBaseTexto(),
                c.getDatosUsados(),
                Calculo.dinero(c.getImporte())
            });
        }

        String pie;
        if (filas.isEmpty()) {
            pie = "No hay empleados activos con datos válidos para calcular.";
        } else {
            pie = filas.size() + " empleado(s) | Total: " + Calculo.dinero(total.doubleValue());
        }
        if (omitidos > 0) {
            pie += " | " + omitidos + " omitido(s) por datos inválidos";
        }
        return new TablaReporte("Remuneraciones individuales (empleados activos)", columnas, filas, pie);
    }

    // ------------------------------------------------------------------
    // 4.4 Resumen de remuneraciones
    // ------------------------------------------------------------------

    /**
     * Suma las remuneraciones de los empleados activos, con subtotal por tipo
     * y total general.
     *
     * @param tipo null = todos los tipos
     */
    public TablaReporte resumenRemuneraciones(String tipo) {
        validarTipo(tipo);
        String[] columnas = {"Tipo de empleado", "Cantidad de empleados", "Subtotal de remuneraciones"};
        List<Object[]> filas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int cantidadTotal = 0;

        for (String t : TIPOS) {
            if (tipo != null && !tipo.equals(t)) {
                continue;
            }
            int cantidad = 0;
            BigDecimal subtotal = BigDecimal.ZERO;
            for (Empleado e : empleadoDAO.listar(t, false)) {
                if (CalculoService.verificar(e) != null) {
                    continue;
                }
                cantidad++;
                subtotal = subtotal.add(BigDecimal.valueOf(CalculoService.construir(e, null).getImporte()));
            }
            filas.add(new Object[]{t, cantidad, Calculo.dinero(subtotal.doubleValue())});
            cantidadTotal += cantidad;
            total = total.add(subtotal);
        }

        String pie;
        if (cantidadTotal == 0) {
            filas.clear();
            pie = "No hay empleados activos con datos válidos para resumir.";
        } else {
            filas.add(new Object[]{"TOTAL GENERAL", cantidadTotal, Calculo.dinero(total.doubleValue())});
            pie = "Total general de remuneraciones: " + Calculo.dinero(total.doubleValue());
        }
        return new TablaReporte("Resumen de remuneraciones (empleados activos)", columnas, filas, pie);
    }

    // ------------------------------------------------------------------
    // 4.5 Historial de cálculos
    // ------------------------------------------------------------------

    /**
     * Muestra los cálculos registrados, sin alterarlos.
     *
     * @param codigoEmpleado código exacto; null o vacío = todos los empleados
     * @param periodo        aaaa-mm; null o vacío = todos los períodos
     */
    public TablaReporte historial(String codigoEmpleado, String periodo) {
        if (periodo != null && !periodo.trim().isEmpty()) {
            String error = CalculoService.validarPeriodo(periodo);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
        }
        List<Calculo> lista = calculoDAO.listar(codigoEmpleado, periodo);

        String[] columnas = {"Período", "Código", "Empleado", "Tipo", "Salario base",
            "Datos usados", "Remuneración calculada", "Fecha de cálculo"};
        List<Object[]> filas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Calculo c : lista) {
            total = total.add(BigDecimal.valueOf(c.getImporte()));
            filas.add(new Object[]{
                c.getPeriodo(),
                c.getCodigoEmpleado(),
                c.getNombreEmpleado(),
                c.getTipo(),
                c.getSalarioBaseTexto(),
                c.getDatosUsados(),
                Calculo.dinero(c.getImporte()),
                c.getFechaCalculo() == null ? "" : c.getFechaCalculo().format(FECHA_HORA)
            });
        }

        String pie = lista.isEmpty()
                ? "No hay cálculos registrados con los criterios indicados."
                : lista.size() + " cálculo(s) | Total: " + Calculo.dinero(total.doubleValue());
        return new TablaReporte("Historial de cálculos de remuneración", columnas, filas, pie);
    }

    // ------------------------------------------------------------------

    private void validarTipo(String tipo) {
        if (tipo == null) {
            return;
        }
        for (String t : TIPOS) {
            if (t.equals(tipo)) {
                return;
            }
        }
        throw new IllegalArgumentException("El tipo de empleado \"" + tipo + "\" no es una opción permitida.");
    }
}
