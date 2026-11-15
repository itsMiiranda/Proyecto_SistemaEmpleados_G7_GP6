package conexion;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class ConexionMongo {

    private static final String URI = "mongodb://localhost:27017/";
    private static final String DATABASE = "empleadodb";

    private static MongoDatabase database;

    public static MongoDatabase getConexion() {
        if (database == null) {
        try {
            MongoClient mongoClient = MongoClients.create(URI);
            MongoDatabase db = mongoClient.getDatabase(DATABASE);
            db.runCommand(new org.bson.Document("ping", 1));//Falla si no hay servidor
            database = db;
            System.out.println("Conexion exitosa a MongoDB");
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo conectar a MongoDB. Verifique que el servicio este en ejecucion.", e);
        }
    }
    return database;
    }
}

