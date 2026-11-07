
package dao;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import modelo.Usuario;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt; // uso de jBCrypt 
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
     * Busca un usuario por su nombre de usuario.
     * Si lo encuentra, devuelve el objeto Usuario.
     * Si no existe, devuelve null.
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
        usuario.setNombre(doc.getString("nombre"));

        return usuario;
    }
    
    /**
     * Verifica si el usuario y la contraseña son correctos,
     * La contraseña que llega aquí está en texto plano,
     * y la comparamos con el hash que está guardado en la base.
     */
    public boolean validarLogin(String nombreUsuario, String passwordPlano) {
        Usuario usuario = buscarPorUsuario(nombreUsuario);

        if (usuario == null) {
            return false; // el usuario no existe
        }

        // Comparamos la contraseña escrita con el hash guardado
        return BCrypt.checkpw(passwordPlano, usuario.getPassword());
    }
    
    
}
