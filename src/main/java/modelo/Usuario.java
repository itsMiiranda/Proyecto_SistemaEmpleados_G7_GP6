
package modelo;
import org.bson.types.ObjectId;

/**
 *
 * @author MINEDUCYT
 */
public class Usuario {
    private ObjectId id;
    private String usuario;
    private String password; // aquí guardamos el hash, y no la contraseña en texto plano
    private String nombre;

    public Usuario() {
    }

    public Usuario(String usuario, String password, String nombre) {
        this.usuario = usuario;
        this.password = password;
        this.nombre = nombre;
    }

    //Getters y Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    
}
