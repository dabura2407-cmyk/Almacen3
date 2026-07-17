package almacen;

import almacen.model.Usuario;
import almacen.service.AlmacenService;
import almacen.ui.*;
import almacen.util.ConexionDB;

import static almacen.ui.Consola.*;

/**
 * Punto de entrada del Sistema Gestor de Almacén de Prendas de Ropa.
 *
 * Credenciales de prueba:
 *   admin        / admin123      → ADMINISTRADOR
 *   almacenista  / almacen123    → ALMACENISTA
 *   despachador  / despacho123   → DESPACHADOR
 */
public class Main {

    public static void main(String[] args) {
        AlmacenService srv = new AlmacenService();

        pantallaBienvenida();

        boolean continuar = true;
        while (continuar) {
            Usuario usuario = iniciarSesion(srv);
            if (usuario == null) {
                continuar = preguntarReintentar();
                continue;
            }

            // Redirigir al menú según rol
            switch (usuario.getRol()) {
                case ADMINISTRADOR -> new MenuAdministrador(srv, usuario).mostrar();
                case ALMACENISTA   -> new MenuAlmacenista(srv, usuario).mostrar();
                case DESPACHADOR   -> new MenuDespachador(srv, usuario).mostrar();
            }

            // Después de cerrar sesión, preguntar si desea volver a entrar
            String resp = leerLinea("\n¿Deseas iniciar sesión con otra cuenta? (s/n): ");
            continuar = resp.equalsIgnoreCase("s");
        }

        ConexionDB.cerrar();
        titulo("SESIÓN FINALIZADA");
        System.out.println("  Gracias por usar el Sistema de Almacén de Prendas.");
        System.out.println();
    }

    // ── Pantalla de bienvenida ─────────────────────────────────
    private static void pantallaBienvenida() {
        limpiar();
        System.out.println(CYAN + BOLD);
        System.out.println("  ╔════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                    ║");
        System.out.println("  ║     🧥  GESTOR DE ALMACÉN DE PRENDAS DE ROPA      ║");
        System.out.println("  ║                                                    ║");
        System.out.println("  ║         Sistema de Control de Inventario           ║");
        System.out.println("  ╚════════════════════════════════════════════════════╝");
        System.out.println(RESET);
    }

    // ── Login ─────────────────────────────────────────────────
    private static Usuario iniciarSesion(AlmacenService srv) {
        titulo("INICIAR SESIÓN");
        String user = leerLinea("  Usuario: ");
        String pass = leerLinea("  Contraseña: ");

        try {
            Usuario u = srv.login(user, pass);
            if (u != null) {
                ok("Bienvenido, " + u.getNombre() + " (" + u.getRol() + ")");
                pausar();
                return u;
            } else {
                error("Credenciales incorrectas o usuario inactivo.");
                return null;
            }
        } catch (Exception e) {
            error("Error de conexión: " + e.getMessage());
            info("Verifica que MySQL esté activo y la BD 'almacen_ropa' exista.");
            return null;
        }
    }

    private static boolean preguntarReintentar() {
        String resp = leerLinea("  ¿Intentar de nuevo? (s/n): ");
        return resp.equalsIgnoreCase("s");
    }
}
