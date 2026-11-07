package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import modelo.Usuario;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;
import conexion.ConexionMongo;

/**
 *
 * @author G7_GP6
 */
public class UsuarioDAO {
   private MongoCollection<Document> coleccion;

    public UsuarioDAO() {
        MongoDatabase db = ConexionMongo.getConexion();
        this.coleccion = db.getCollection("usuarios");
    }

    /**
     * Busca un usuario por el campo "usuario"
     */
    public Usuario buscarPorUsuario(String nombreUsuario) {
        Document doc = coleccion.find(Filters.eq("usuario", nombreUsuario)).first();

        if (doc == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setId(doc.getObjectId("_id"));
        usuario.setUsuario(doc.getString("usuario"));
        usuario.setPassword(doc.getString("password"));
        usuario.setRol(doc.getString("rol"));
        usuario.setActivo(doc.getBoolean("activo", true));

        return usuario;
    }

    /**
     * Valida el login.
     * Compara la contraseña que escribe el usuario
     * con el hash que está guardado en la base de datos.
     */
    
    public boolean validarLogin(String nombreUsuario, String passwordPlano) {
        Usuario usuario = buscarPorUsuario(nombreUsuario);

        if (usuario == null) {
            return false;
        }

        if (!usuario.isActivo()) {
            return false;
        }

        return BCrypt.checkpw(passwordPlano, usuario.getPassword());
    }
    
}
