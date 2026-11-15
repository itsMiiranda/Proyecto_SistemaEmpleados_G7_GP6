
package util;

/*
 * @author G7_GP6
 */

public class Validador {

    // Verifica que el campo no esté vacío
    public static boolean campoVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    // Verifica que el valor sea numérico
    public static boolean esNumero(String texto) {
        try {
            Double.parseDouble(texto);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Verifica que el número no sea negativo
    public static boolean esPositivo(String texto) {
        if (!esNumero(texto)) {
            return false;
        }

        return Double.parseDouble(texto) >= 0;
    }

    // Verifica que el porcentaje esté entre 0 y 100
    public static boolean porcentajeValido(String texto) {
        if (!esNumero(texto)) {
            return false;
        }

        double porcentaje = Double.parseDouble(texto);
        return porcentaje >= 0 && porcentaje <= 100;
    }
}

