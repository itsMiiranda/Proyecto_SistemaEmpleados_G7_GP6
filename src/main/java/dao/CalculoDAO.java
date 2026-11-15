package dao;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import conexion.ConexionMongo;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import modelo.Calculo;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Acceso a datos de la colección "calculos" (historial de remuneraciones).
 * El historial es de solo lectura: solo se insertan cálculos nuevos, nunca se
 * modifican ni se eliminan los anteriores.
 *
 * @author G7_GP6
 */
public class CalculoDAO {

    private final MongoCollection<Document> coleccion;

    public CalculoDAO() {
        MongoDatabase db = ConexionMongo.getConexion();
        this.coleccion = db.getCollection("calculos");
        asegurarIndiceUnico();
    }

    /**
     * Un empleado solo puede tener un cálculo por período. El índice lo garantiza
     * también a nivel de base de datos. Si no se pudo crear, la validación de
     * existe() en el servicio sigue protegiendo la regla.
     */
    private void asegurarIndiceUnico() {
        try {
            coleccion.createIndex(Indexes.ascending("codigoEmpleado", "periodo"),
                    new IndexOptions().unique(true));
        } catch (MongoException e) {
            System.err.println("Aviso: no se pudo crear el índice único de cálculos: " + e.getMessage());
        }
    }

    /** Indica si ya existe un cálculo registrado para el empleado en ese período. */
    public boolean existe(String codigoEmpleado, String periodo) {
        try {
            return coleccion.countDocuments(Filters.and(
                    Filters.eq("codigoEmpleado", codigoEmpleado),
                    Filters.eq("periodo", periodo))) > 0;
        } catch (MongoException e) {
            throw new RuntimeException("Error al consultar MongoDB: " + e.getMessage(), e);
        }
    }

    /**
     * Registra un cálculo en el historial.
     *
     * @throws IllegalArgumentException si ya existe un cálculo del empleado para ese período
     */
    public void insertar(Calculo c) {
        Document doc = new Document("codigoEmpleado", c.getCodigoEmpleado())
                .append("nombreEmpleado", c.getNombreEmpleado())
                .append("tipo", c.getTipo())
                .append("periodo", c.getPeriodo());

        if (c.getSalarioBase() != null) {
            doc.append("salarioBase", c.getSalarioBase());
        }
        if (c.getTarifaHora() != null) {
            doc.append("tarifaHora", c.getTarifaHora());
        }
        if (c.getHorasTrabajadas() != null) {
            doc.append("horasTrabajadas", c.getHorasTrabajadas());
        }
        if (c.getMontoVentas() != null) {
            doc.append("montoVentas", c.getMontoVentas());
        }
        if (c.getPorcentajeComision() != null) {
            doc.append("porcentajeComision", c.getPorcentajeComision());
        }
        doc.append("importe", c.getImporte())
                .append("fechaCalculo", aFecha(c.getFechaCalculo()));

        try {
            coleccion.insertOne(doc);
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                throw new IllegalArgumentException("Ya existe un cálculo registrado para el empleado "
                        + c.getCodigoEmpleado() + " en el período " + c.getPeriodo() + ".");
            }
            throw new RuntimeException("Error al guardar en MongoDB: " + e.getMessage(), e);
        } catch (MongoException e) {
            throw new RuntimeException("Error al guardar en MongoDB: " + e.getMessage(), e);
        }
    }

    /**
     * Consulta el historial.
     *
     * @param codigoEmpleado código exacto del empleado; null o vacío = todos
     * @param periodo        período aaaa-mm; null o vacío = todos
     */
    public List<Calculo> listar(String codigoEmpleado, String periodo) {
        List<Bson> filtros = new ArrayList<>();
        if (codigoEmpleado != null && !codigoEmpleado.trim().isEmpty()) {
            filtros.add(Filters.eq("codigoEmpleado", codigoEmpleado.trim()));
        }
        if (periodo != null && !periodo.trim().isEmpty()) {
            filtros.add(Filters.eq("periodo", periodo.trim()));
        }
        Bson filtro = filtros.isEmpty() ? new Document() : Filters.and(filtros);

        try {
            List<Calculo> resultado = new ArrayList<>();
            for (Document doc : coleccion.find(filtro)
                    .sort(Sorts.orderBy(Sorts.descending("periodo"), Sorts.ascending("codigoEmpleado")))) {
                resultado.add(aCalculo(doc));
            }
            return resultado;
        } catch (MongoException e) {
            throw new RuntimeException("Error al consultar MongoDB: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // Conversión Documento -> Calculo
    // ------------------------------------------------------------------

    private Calculo aCalculo(Document doc) {
        Calculo c = new Calculo();
        c.setCodigoEmpleado(doc.getString("codigoEmpleado"));
        c.setNombreEmpleado(doc.getString("nombreEmpleado"));
        c.setTipo(doc.getString("tipo"));
        c.setPeriodo(doc.getString("periodo"));
        c.setSalarioBase(decimal(doc, "salarioBase"));
        c.setTarifaHora(decimal(doc, "tarifaHora"));
        c.setHorasTrabajadas(decimal(doc, "horasTrabajadas"));
        c.setMontoVentas(decimal(doc, "montoVentas"));
        c.setPorcentajeComision(decimal(doc, "porcentajeComision"));
        Double importe = decimal(doc, "importe");
        c.setImporte(importe == null ? 0.0 : importe);
        c.setFechaCalculo(aLocalDateTime(doc.getDate("fechaCalculo")));
        return c;
    }

    // Devuelve null si el campo no existe (no aplica a ese tipo de empleado)
    private Double decimal(Document doc, String campo) {
        Object valor = doc.get(campo);
        return valor instanceof Number ? ((Number) valor).doubleValue() : null;
    }

    private Date aFecha(LocalDateTime fecha) {
        return fecha == null ? null : Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime aLocalDateTime(Date fecha) {
        return fecha == null ? null : fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
