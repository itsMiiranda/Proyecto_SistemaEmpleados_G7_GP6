package servicio;

import java.util.List;

/**
 * Resultado de un reporte: título, columnas, filas y una línea final con
 * totales o mensajes. La pantalla solo lo muestra y el exportador lo guarda,
 * sin conocer cómo se calculó.
 *
 * @author G7_GP6
 */
public class TablaReporte {

    private final String titulo;
    private final String[] columnas;
    private final List<Object[]> filas;
    private final String pie;

    public TablaReporte(String titulo, String[] columnas, List<Object[]> filas, String pie) {
        this.titulo = titulo;
        this.columnas = columnas;
        this.filas = filas;
        this.pie = pie;
    }

    public String getTitulo() {
        return titulo;
    }

    public String[] getColumnas() {
        return columnas;
    }

    public List<Object[]> getFilas() {
        return filas;
    }

    public String getPie() {
        return pie;
    }

    public boolean estaVacia() {
        return filas.isEmpty();
    }
}
