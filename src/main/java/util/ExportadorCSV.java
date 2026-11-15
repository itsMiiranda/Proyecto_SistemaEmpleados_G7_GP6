package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Exporta una tabla de reporte a un archivo CSV (UTF-8 con BOM para que Excel
 * muestre bien las tildes). Todos los campos se entrecomillan.
 *
 * @author G7_GP6
 */
public class ExportadorCSV {

    private ExportadorCSV() {
    }

    public static void exportar(File archivo, String[] columnas, List<Object[]> filas) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(archivo.toPath(), StandardCharsets.UTF_8)) {
            w.write('﻿');
            w.write(linea(columnas));
            for (Object[] fila : filas) {
                w.write(linea(fila));
            }
        }
    }

    private static String linea(Object[] campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            String texto = campos[i] == null ? "" : String.valueOf(campos[i]);
            sb.append('"').append(texto.replace("\"", "\"\"")).append('"');
        }
        return sb.append("\r\n").toString();
    }
}
