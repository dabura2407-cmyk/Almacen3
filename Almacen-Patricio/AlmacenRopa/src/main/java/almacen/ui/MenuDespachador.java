package almacen.ui;

import almacen.model.*;
import almacen.service.AlmacenService;

import java.sql.SQLException;
import java.util.List;

import static almacen.ui.Consola.*;

/** Interfaz de menús para el rol DESPACHADOR. */
public class MenuDespachador {

    private final AlmacenService srv;
    private final Usuario sesion;

    public MenuDespachador(AlmacenService srv, Usuario sesion) {
        this.srv    = srv;
        this.sesion = sesion;
    }

    public void mostrar() {
        boolean salir = false;
        while (!salir) {
            titulo("MENÚ DESPACHADOR  — " + sesion.getNombre());
            System.out.println("  1. Consultar inventario");
            System.out.println("  2. Registrar despacho");
            System.out.println("  3. Ver mis despachos");
            System.out.println("  0. Cerrar sesión");
            separador();
            switch (leerOpcion(0, 3)) {
                case 1 -> consultarInventario();
                case 2 -> registrarDespacho();
                case 3 -> verMisDespachos();
                case 0 -> salir = true;
            }
        }
    }

    // ── Inventario ────────────────────────────────────────────
    private void consultarInventario() {
        titulo("CONSULTA DE INVENTARIO");
        try {
            List<Producto> lista = srv.consultarInventario(sesion);
            if (lista.isEmpty()) { info("Sin productos en inventario."); pausar(); return; }
            System.out.printf("  %-5s %-8s %-25s %-10s %-6s %-8s%n",
                    "ID","Código","Nombre","Categoría","Talla","Stock");
            separador();
            for (Producto p : lista) {
                String disponible = p.getStockActual() > 0
                        ? VERDE + p.getStockActual() + RESET
                        : ROJO  + "0 (SIN STOCK)" + RESET;
                System.out.printf("  %-5d %-8s %-25s %-10s %-6s %s%n",
                        p.getId(), p.getCodigo(), p.getNombre(),
                        p.getCategoriaNombre(), p.getTalla(), disponible);
            }

            // Opción de búsqueda
            String buscar = leerLinea("\n  Buscar producto por nombre (ENTER para omitir): ");
            if (!buscar.isEmpty()) {
                List<Producto> res = srv.buscarProductoPorNombre(sesion, buscar);
                if (res.isEmpty()) info("Sin resultados para: " + buscar);
                else res.forEach(p -> System.out.println("  → " + p));
            }
        } catch (SQLException e) { error("Error DB: " + e.getMessage()); }
        pausar();
    }

    // ── Despacho ──────────────────────────────────────────────
    private void registrarDespacho() {
        titulo("REGISTRAR DESPACHO");
        try {
            int productoId   = leerEntero("  ID del producto a despachar: ");
            int cantidad      = leerEntero("  Cantidad a despachar: ");
            String destinatario = leerLinea("  Destinatario / área: ");
            String observacion  = leerLinea("  Observación (opcional): ");

            Despacho d = srv.realizarDespacho(sesion, productoId, cantidad, destinatario, observacion);

            if (d.getEstado() == Despacho.Estado.DESPACHADO) {
                ok("Despacho registrado exitosamente.");
                System.out.println("  Folio : " + CYAN + d.getFolio() + RESET);
                System.out.println("  Estado: " + VERDE + "DESPACHADO" + RESET);
            } else {
                error("NO SE CUENTA CON STOCK SUFICIENTE.");
                System.out.println("  Folio     : " + CYAN + d.getFolio() + RESET);
                System.out.println("  Estado    : " + ROJO + "SIN STOCK" + RESET);
                System.out.println("  Detalle   : " + d.getObservacion());
                info("Se registró la aclaración de falta de stock.");
            }
        } catch (Exception e) { error("Error: " + e.getMessage()); }
        pausar();
    }

    // ── Historial ─────────────────────────────────────────────
    private void verMisDespachos() {
        titulo("MIS DESPACHOS");
        try {
            List<Despacho> lista = srv.misDespachos(sesion);
            if (lista.isEmpty()) { info("No tienes despachos registrados."); pausar(); return; }
            System.out.printf("  %-18s %-22s %-6s %-20s %-12s%n",
                    "Folio","Producto","Cant.","Destinatario","Estado");
            separador();
            for (Despacho d : lista) {
                String color = d.getEstado() == Despacho.Estado.DESPACHADO ? VERDE : ROJO;
                System.out.printf("  %-18s %-22s %-6d %-20s %s%s%s%n",
                        d.getFolio(), d.getProductoNombre(),
                        d.getCantidad(), d.getDestinatario(),
                        color, d.getEstado(), RESET);
            }
        } catch (SQLException e) { error("Error DB: " + e.getMessage()); }
        pausar();
    }
}
