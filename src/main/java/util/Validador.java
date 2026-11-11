package util;

public class Validador {

  // Verifica que el campo no este vacio
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

  // Verifica que el el numero no sea negativo 
  public static boolean esPositivo(String texto) {
    if (!esNumero(texto)) {
      return false;
    }

    return Double.parseDouble(texto) >= 0;
  }

  //Verifica el porcentaje de comision
  public static boolean porcentajeValido(String texto) {
    if (!esNumero(texto)) {
      return false; 
    }

    double porcentaje = Double.parseDouble(texto);
    return porcentaje >= 0 && porcentaje <= 100; 
  }
}
