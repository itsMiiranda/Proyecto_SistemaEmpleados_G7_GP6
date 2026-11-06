
package app;

import org.mindrot.jbcrypt.BCrypt;
/**
 *
 * @author G7_GP6
 */

public class GenerarHash {
    public static void main(String[] args) {
        System.out.println(BCrypt.hashpw("admin123", BCrypt.gensalt()));
    }
}
