package almacen.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestiona la conexión a la base de datos MySQL/MariaDB.
 * Ajusta URL, USER y PASSWORD según tu entorno.
 */
public class ConexionDB {

    private static final String URL      = "jdbc:mysql://localhost:3306/almacen_ropa?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = ""; // Cambia según tu instalación

    private static Connection instancia = null;

    private ConexionDB() {}

    /** Devuelve (o crea) la conexión singleton. */
    public static Connection getConexion() throws SQLException {
        if (instancia == null || instancia.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver JDBC no encontrado: " + e.getMessage());
            }
            instancia = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return instancia;
    }

    /** Cierra la conexión si está abierta. */
    public static void cerrar() {
        if (instancia != null) {
            try {
                if (!instancia.isClosed()) instancia.close();
            } catch (SQLException ignored) {}
            instancia = null;
        }
    }

    /**
     * Ejecuta la acción dada dentro de una transacción: desactiva autocommit,
     * hace commit si todo sale bien, o rollback si se lanza cualquier excepción.
     * Al final siempre restaura autocommit=true.
     */
    public static <T> T ejecutarEnTransaccion(AccionTransaccional<T> accion) throws SQLException {
        Connection conn = getConexion();
        boolean autoCommitOriginal = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            T resultado = accion.ejecutar();
            conn.commit();
            return resultado;
        } catch (Exception e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            if (e instanceof SQLException se) throw se;
            throw new SQLException("Error en transacción: " + e.getMessage(), e);
        } finally {
            try { conn.setAutoCommit(autoCommitOriginal); } catch (SQLException ignored) {}
        }
    }

    /** Función funcional que representa el trabajo a ejecutar dentro de la transacción. */
    @FunctionalInterface
    public interface AccionTransaccional<T> {
        T ejecutar() throws Exception;
    }
}
