package almacen.ui;

import almacen.model.*;
import almacen.service.AlmacenService;

import java.sql.SQLException;
import java.util.List;

import static almacen.ui.Consola.*;

/** Interfaz de menús para el rol ALMACENISTA. */
public class MenuAlmacenista {

    private final AlmacenService srv;
    private final Usuario sesion;

    public MenuAlmacenista(AlmacenService srv, Usuario sesion) {
        this.srv    = srv;
        this.sesion = sesion;
    }

    public void mostrar() {
        boolean salir = false;
        while (!salir) {
            titulo("MENÚ ALMACENISTA  — " + sesion.getNombre());
            System.out.println("  1. Consultar inventario");
            System.out.println("  2. Solicitar ALTA de inventario");
            System.out.println("  3. Solicitar BAJA de inventario");
            System.out.println("  4. Ver mis solicitudes");
            System.out.println("  0. Cerrar sesión");
            separador();
            switch (leerOpcion(0, 4)) {
                case 1 -> consultarInventario();
                case 2 -> crearSolicitud(Solicitud.Tipo.ALTA);
                case 3 -> crearSolicitud(Solicitud.Tipo.BAJA);
                case 4 -> verMisSolicitudes();
                case 0 -> salir = true;
            }
        }
    }

    // ── Inventario ────────────────────────────────────────────
    private void consultarInventario() {
        titulo("INVENTARIO ACTUAL");
        try {
            List<Producto> lista = srv.consultarInventario(sesion);
            if (lista.isEmpty()) { info("Sin productos registrados."); pausar(); return; }
            System.out.printf("  %-8s %-25s %-10s %-6s %-8s%n",
                    "Código","Nombre","Categoría","Talla","Stock");
            separador();
            for (Producto p : lista) {
                String alerta = p.stockBajo() ? ROJO + " ⚠ BAJO" + RESET : "";
                System.out.printf("  %-8s %-25s %-10s %-6s %-8d%s%n",
                        p.getCodigo(), p.getNombre(), p.getCategoriaNombre(),
                        p.getTalla(), p.getStockActual(), alerta);
            }
            info("⚠ = stock por debajo del mínimo");
        } catch (SQLException e) { error("Error DB: " + e.getMessage()); }
        pausar();
    }

    // ── Solicitudes ───────────────────────────────────────────
    private void crearSolicitud(Solicitud.Tipo tipo) {
        titulo("SOLICITUD DE " + tipo);
        try {
            // Mostrar inventario para facilitar la elección
            List<Producto> lista = srv.consultarInventario(sesion);
            System.out.printf("  %-5s %-8s %-25s %-8s%n","ID","Código","Nombre","Stock");
            separador();
            for (Producto p : lista) {
                System.out.printf("  %-5d %-8s %-25s %-8d%n",
                        p.getId(), p.getCodigo(), p.getNombre(), p.getStockActual());
            }
            separador();
            int productoId = leerEntero("  ID del producto: ");
            int cantidad   = leerEntero("  Cantidad: ");
            String motivo  = leerLinea("  Motivo de la solicitud: ");

            int id = srv.crearSolicitud(sesion, productoId, tipo, cantidad, motivo);
            if (id > 0) ok("Solicitud #" + id + " creada. Queda PENDIENTE de aprobación por el administrador.");
            else        error("No se pudo crear la solicitud.");
        } catch (Exception e) { error("Error: " + e.getMessage()); }
        pausar();
    }

    private void verMisSolicitudes() {
        titulo("MIS SOLICITUDES");
        try {
            List<Solicitud> lista = srv.misSolicitudes(sesion);
            if (lista.isEmpty()) { info("No tienes solicitudes registradas."); pausar(); return; }
            for (Solicitud s : lista) {
                String color = switch (s.getEstado()) {
                    case APROBADA  -> VERDE;
                    case RECHAZADA -> ROJO;
                    default        -> AMARILLO;
                };
                System.out.printf("  [%d] %s | %s%s%s x%d | %s | %s%n",
                        s.getId(),
                        s.getCreadoEn().toLocalDate(),
                        color, s.getEstado(), RESET,
                        s.getCantidad(),
                        s.getProductoNombre(),
                        s.getMotivo() != null ? s.getMotivo() : "");
                if (s.getObservacion() != null && !s.getObservacion().isEmpty()) {
                    System.out.println("       → Obs: " + s.getObservacion());
                }
            }
        } catch (SQLException e) { error("Error DB: " + e.getMessage()); }
        pausar();
    }
}
