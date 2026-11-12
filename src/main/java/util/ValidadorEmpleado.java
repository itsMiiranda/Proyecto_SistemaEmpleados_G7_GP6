package util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validaciones de los datos de entrada del formulario de empleados (RF-04).
 * Cada método devuelve un mensaje de error comprensible, o null si el dato es válido.
 *
 * @author G7_GP6
 */
public class ValidadorEmpleado {

    // Formato del documento definido por el equipo: 8 dígitos, guion y 1 dígito (ej. 01234567-8)
    private static final Pattern DOCUMENTO = Pattern.compile("\\d{8}-\\d");

    private ValidadorEmpleado() {
    }

    /** Valida un campo de texto obligatorio. */
    public static String obligatorio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            return "El campo \"" + campo + "\" es obligatorio.";
        }
        return null;
    }

    /** Valida el formato del documento de identificación (########-#). */
    public static String documento(String valor) {
        String error = obligatorio(valor, "Documento");
        if (error != null) {
            return error;
        }
        if (!DOCUMENTO.matcher(valor.trim()).matches()) {
            return "El documento debe tener el formato ########-# (ejemplo: 01234567-8).";
        }
        return null;
    }

    /** Valida que la fecha de ingreso exista, tenga formato aaaa-mm-dd y no sea futura. */
    public static String fechaIngreso(String valor) {
        String error = obligatorio(valor, "Fecha de ingreso");
        if (error != null) {
            return error;
        }
        try {
            LocalDate fecha = LocalDate.parse(valor.trim());
            if (fecha.isAfter(LocalDate.now())) {
                return "La fecha de ingreso no puede ser posterior a hoy.";
            }
        } catch (DateTimeParseException e) {
            return "La fecha de ingreso no es válida. Use el formato aaaa-mm-dd (ejemplo: 2024-03-15).";
        }
        return null;
    }

    /** Valida que el tipo de empleado haya sido seleccionado. */
    public static String tipo(Object seleccionado) {
        if (seleccionado == null) {
            return "Debe seleccionar un tipo de empleado.";
        }
        return null;
    }

    /** Valida un número no negativo (salario, tarifa, horas, ventas). */
    public static String numeroNoNegativo(String valor, String campo) {
        String error = obligatorio(valor, campo);
        if (error != null) {
            return error;
        }
        Double numero = aNumero(valor);
        if (numero == null) {
            return "El campo \"" + campo + "\" debe ser un número (ejemplo: 1200.50).";
        }
        if (numero < 0) {
            return "El campo \"" + campo + "\" no puede ser negativo.";
        }
        return null;
    }

    /** Valida el porcentaje de comisión, que debe estar entre 0 y 100. */
    public static String porcentaje(String valor) {
        String error = obligatorio(valor, "Porcentaje de comisión");
        if (error != null) {
            return error;
        }
        Double numero = aNumero(valor);
        if (numero == null) {
            return "El porcentaje de comisión debe ser un número (ejemplo: 7.5).";
        }
        if (numero < 0 || numero > 100) {
            return "El porcentaje de comisión debe estar entre 0 y 100.";
        }
        return null;
    }

    /**
     * Convierte un texto a número aceptando coma o punto decimal.
     * Devuelve null si no es un número válido.
     */
    public static Double aNumero(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            double numero = Double.parseDouble(valor.trim().replace(',', '.'));
            if (Double.isNaN(numero) || Double.isInfinite(numero)) {
                return null;
            }
            return numero;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
