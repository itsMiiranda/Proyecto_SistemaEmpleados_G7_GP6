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
            MongoClient mongoClient = MongoClients.create(URI);
            database = mongoClient.getDatabase(DATABASE);
            database.runCommand(new org.bson.Document("ping", 1)); // falla si no hay servidor
            System.out.println("Conexion exitosa a MongoDB");
        }
        return database;
    }
}
