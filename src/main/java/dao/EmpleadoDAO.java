package dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import conexion.ConexionMongo;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import modelo.Empleado;
import modelo.EmpleadoAdministrativo;
import modelo.EmpleadoPorComision;
import modelo.EmpleadoPorHora;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Acceso a datos de la colección "empleados" en MongoDB.
 * Convierte entre documentos de Mongo y las clases del modelo
 * (Empleado y sus clases derivadas).
 *
 * @author G7_GP6
 */
public class EmpleadoDAO {

    public static final String ADMINISTRATIVO = "ADMINISTRATIVO";
    public static final String POR_HORA = "POR_HORA";
    public static final String POR_COMISION = "POR_COMISION";

    private final MongoCollection<Document> coleccion;

    public EmpleadoDAO() {
        MongoDatabase db = ConexionMongo.getConexion();
        this.coleccion = db.getCollection("empleados");
    }

    // ------------------------------------------------------------------
    // Registrar
    // ------------------------------------------------------------------

    /** Indica si ya existe un empleado con ese código. */
    public boolean existeCodigo(String codigo) {
        try {
            return coleccion.countDocuments(Filters.eq("codigo", codigo)) > 0;
        } catch (MongoException e) {
            throw new RuntimeException("Error al consultar MongoDB: " + e.getMessage(), e);
        }
    }

    /**
     * Inserta un nuevo empleado.
     *
     * @throws IllegalArgumentException si el código ya existe
     */
    public void insertar(Empleado empleado) {
        if (existeCodigo(empleado.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un empleado con el código " + empleado.getCodigo() + ".");
        }
        try {
            coleccion.insertOne(aDocumento(empleado));
        } catch (MongoException e) {
            throw new RuntimeException("Error al guardar en MongoDB: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // Buscar y consultar
    // ------------------------------------------------------------------

    /** Busca un empleado por su código exacto. Devuelve null si no existe. */
    public Empleado buscarPorCodigo(String codigo) {
        try {
            Document doc = coleccion.find(Filters.eq("codigo", codigo)).first();
            return doc == null ? null : aEmpleado(doc);
        } catch (MongoException e) {
            throw new RuntimeException("Error al consultar MongoDB: " + e.getMessage(), e);
        }
    }

    /** Busca un empleado por su documento exacto. Devuelve null si no existe. */
    public Empleado buscarPorDocumento(String documento) {
        try {
            Document doc = coleccion.find(Filters.eq("documento", documento)).first();
            return doc == null ? null : aEmpleado(doc);
        } catch (MongoException e) {
            throw new RuntimeException("Error al consultar MongoDB: " + e.getMessage(), e);
        }
    }

    /**
     * Busca empleados cuyo código, documento o nombre contengan el texto
     * (sin distinguir mayúsculas), con filtro opcional por tipo.
     *
     * @param texto            texto a buscar; vacío o null = sin criterio de búsqueda
     * @param tipo             ADMINISTRATIVO, POR_HORA, POR_COMISION; null = todos
     * @param incluirInactivos si es false solo devuelve empleados activos
     */
    public List<Empleado> buscar(String texto, String tipo, boolean incluirInactivos) {
        List<Bson> filtros = new ArrayList<>();

        if (texto != null && !texto.trim().isEmpty()) {
            Pattern patron = Pattern.compile(Pattern.quote(texto.trim()), Pattern.CASE_INSENSITIVE);
            filtros.add(Filters.or(
                    Filters.regex("codigo", patron),
                    Filters.regex("documento", patron),
                    Filters.regex("nombre", patron),
                    Filters.regex("apellido", patron)));
        }
        if (tipo != null) {
            filtros.add(Filters.eq("tipo", tipo));
        }
        if (!incluirInactivos) {
            filtros.add(Filters.eq("activo", true));
        }

        Bson filtro = filtros.isEmpty() ? new Document() : Filters.and(filtros);

        try {
            List<Empleado> resultado = new ArrayList<>();
            for (Document doc : coleccion.find(filtro).sort(Sorts.ascending("codigo"))) {
                Empleado e = aEmpleado(doc);
                if (e != null) {
                    resultado.add(e);
                }
            }
            return resultado;
        } catch (MongoException e) {
            throw new RuntimeException("Error al consultar MongoDB: " + e.getMessage(), e);
        }
    }

    /** Lista todos los empleados, con filtro opcional por tipo. */
    public List<Empleado> listar(String tipo, boolean incluirInactivos) {
        return buscar(null, tipo, incluirInactivos);
    }

    // ------------------------------------------------------------------
    // Modificar e inactivar
    // ------------------------------------------------------------------

    /**
     * Actualiza los datos de un empleado existente. El código y el tipo no
     * se modifican (el tipo define las reglas de cálculo).
     *
     * @return true si se modificó; false si el empleado no existe
     */
    public boolean modificar(Empleado empleado) {
        List<Bson> cambios = new ArrayList<>();
        cambios.add(Updates.set("nombre", empleado.getNombre()));
        cambios.add(Updates.set("apellido", empleado.getApellido()));
        cambios.add(Updates.set("documento", empleado.getDocumento()));
        cambios.add(Updates.set("cargo", empleado.getCargo()));
        cambios.add(Updates.set("fechaIngreso", aFecha(empleado.getFechaIngreso())));

        if (empleado instanceof EmpleadoAdministrativo) {
            cambios.add(Updates.set("salarioBase", ((EmpleadoAdministrativo) empleado).getSalarioBase()));
        } else if (empleado instanceof EmpleadoPorHora) {
            EmpleadoPorHora h = (EmpleadoPorHora) empleado;
            cambios.add(Updates.set("tarifaHora", h.getTarifaPorHora()));
            cambios.add(Updates.set("horasTrabajadas", h.getHorasTrabajadas()));
        } else if (empleado instanceof EmpleadoPorComision) {
            EmpleadoPorComision c = (EmpleadoPorComision) empleado;
            cambios.add(Updates.set("montoVentas", c.getMontoVentas()));
            cambios.add(Updates.set("porcentajeComision", c.getPorcentajeComision()));
        }

        try {
            UpdateResult r = coleccion.updateOne(Filters.eq("codigo", empleado.getCodigo()), Updates.combine(cambios));
            return r.getMatchedCount() > 0;
        } catch (MongoException e) {
            throw new RuntimeException("Error al modificar en MongoDB: " + e.getMessage(), e);
        }
    }

    /**
     * Inactiva un empleado (no se elimina: se conserva su información histórica).
     *
     * @return true si el empleado existe; false si no se encontró
     */
    public boolean inactivar(String codigo) {
        try {
            UpdateResult r = coleccion.updateOne(Filters.eq("codigo", codigo), Updates.set("activo", false));
            return r.getMatchedCount() > 0;
        } catch (MongoException e) {
            throw new RuntimeException("Error al inactivar en MongoDB: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // Conversión Documento <-> Empleado
    // ------------------------------------------------------------------

    private Document aDocumento(Empleado e) {
        Document doc = new Document("codigo", e.getCodigo())
                .append("nombre", e.getNombre())
                .append("apellido", e.getApellido())
                .append("documento", e.getDocumento())
                .append("cargo", e.getCargo())
                .append("fechaIngreso", aFecha(e.getFechaIngreso()))
                .append("tipo", e.getTipo())
                .append("activo", e.isActivo());

        if (e instanceof EmpleadoAdministrativo) {
            doc.append("salarioBase", ((EmpleadoAdministrativo) e).getSalarioBase());
        } else if (e instanceof EmpleadoPorHora) {
            EmpleadoPorHora h = (EmpleadoPorHora) e;
            doc.append("tarifaHora", h.getTarifaPorHora()).append("horasTrabajadas", h.getHorasTrabajadas());
        } else if (e instanceof EmpleadoPorComision) {
            EmpleadoPorComision c = (EmpleadoPorComision) e;
            doc.append("montoVentas", c.getMontoVentas()).append("porcentajeComision", c.getPorcentajeComision());
        }
        return doc;
    }

    /** Crea el objeto del modelo según el campo "tipo". Devuelve null si el tipo es desconocido. */
    private Empleado aEmpleado(Document doc) {
        String tipo = doc.getString("tipo");
        Empleado e;

        if (ADMINISTRATIVO.equals(tipo)) {
            EmpleadoAdministrativo a = new EmpleadoAdministrativo();
            a.setSalarioBase(numero(doc, "salarioBase"));
            e = a;
        } else if (POR_HORA.equals(tipo)) {
            EmpleadoPorHora h = new EmpleadoPorHora();
            h.setTarifaPorHora(numero(doc, "tarifaHora"));
            h.setHorasTrabajadas(numero(doc, "horasTrabajadas"));
            e = h;
        } else if (POR_COMISION.equals(tipo)) {
            EmpleadoPorComision c = new EmpleadoPorComision();
            c.setMontoVentas(numero(doc, "montoVentas"));
            c.setPorcentajeComision(numero(doc, "porcentajeComision"));
            e = c;
        } else {
            return null; // tipo no interpretable: se omite
        }

        e.setCodigo(doc.getString("codigo"));
        e.setNombre(doc.getString("nombre") == null ? "" : doc.getString("nombre"));
        e.setApellido(doc.getString("apellido") == null ? "" : doc.getString("apellido"));
        e.setDocumento(doc.getString("documento"));
        e.setCargo(doc.getString("cargo"));
        e.setFechaIngreso(aLocalDate(doc.getDate("fechaIngreso")));
        e.setActivo(doc.getBoolean("activo", true));
        e.setTipo(tipo);
        return e;
    }

    // Lee un número sin importar si Mongo lo guardó como int, long o double
    private double numero(Document doc, String campo) {
        Object valor = doc.get(campo);
        return valor instanceof Number ? ((Number) valor).doubleValue() : 0.0;
    }

    // Se usa UTC en ambas conversiones para que la fecha no cambie de día por la zona horaria
    private Date aFecha(LocalDate fecha) {
        return fecha == null ? null : Date.from(fecha.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    private LocalDate aLocalDate(Date fecha) {
        return fecha == null ? null : fecha.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }
}
