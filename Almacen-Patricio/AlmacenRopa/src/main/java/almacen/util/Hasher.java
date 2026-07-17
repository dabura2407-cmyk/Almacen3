package almacen.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Utilidad para cifrar contraseñas con SHA-256. */
public class Hasher {

    private Hasher() {}

    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(texto.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }
}
