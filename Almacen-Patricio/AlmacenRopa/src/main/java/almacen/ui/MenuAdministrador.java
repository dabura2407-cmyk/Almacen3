package almacen.ui;

import almacen.model.*;
import almacen.service.AlmacenService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static almacen.ui.Consola.*;

/** Interfaz de menús para el rol ADMINISTRADOR. */
public class MenuAdministrador {

    private final AlmacenService srv;
    private final Usuario sesion;

    public MenuAdministrador(AlmacenService srv, Usuario sesion) {
        this.srv    = srv;
        this.sesion = sesion;
    }

    public void mostrar() {
        boolean salir = false;
        while (!salir) {
            titulo("MENÚ ADMINISTRADOR  — " + sesion.getNombre());
            System.out.println("  1. Gestión de inventario (productos)");
            System.out.println("  2. Solicitudes pendientes (aprobar/rechazar)");
            System.out.println("  3. Ver todas las solicitudes");
            System.out.println("  4. Ver todos los despachos");
            System.out.println("  5. Gestión de usuarios");
            System.out.println("  6. Alertas de stock bajo");
            System.out.println("  0. Cerrar sesión");
            separador();
            switch (leerOpcion(0, 6)) {
                case 1 -> menuProductos();
                case 2 -> menuAprobarSolicitudes();
                case 3 -> verTodasSolicitudes();
                case 4 -> verTodosDespachos();
                case 5 -> menuUsuarios();
                case 6 -> alertasStockBajo();
                case 0 -> salir = true;
            }
        }
    }

    // ── Productos ─────────────────────────────────────────────
    private void menuProductos() {
        titulo("GESTIÓN DE PRODUCTOS");
        System.out.println("  1. Ver inventario completo");
        System.out.println("  2. Agregar producto");
        System.out.println("  3. Editar producto");
        System.out.println("  0. Regresar");
        separador();
        switch (leerOpcion(0, 3)) {
            case 1 -> verInventario();
            case 2 -> agregarProducto();
            case 3 -> editarProducto();
        }
    }

    private void verInventario() {
        titulo("INVENTARIO ACTUAL");
        try {
            List<Producto> lista = srv.consultarInventario(sesion);
            if (lista.isEmpty()) { info("Sin productos registrados."); pausar(); return; }
            System.out.printf("  %-8s %-25s %-10s %-8s %-8s %-8s%n",
                    "Código","Nombre","Categoría","Talla","Stock","Precio");
            separador();
            for (Producto p : lista) {
                String alerta = p.stockBajo() ? ROJO + " ⚠" + RESET : "";
                System.out.printf("  %-8s %-25s %-10s %-8s %-8d $%-7.2f%s%n",
                        p.getCodigo(), p.getNombre(), p.getCategoriaNombre(),
                        p.getTalla(), p.getStockActual(), p.getPrecioUnitario(), alerta);
            }
        } catch (SQLException e) { error("Error DB: " + e.getMessage()); }
        pausar();
    }

    private void agregarProducto() {
        titulo("AGREGAR PRODUCTO");
        try {
            Producto p = new Producto();
            p.setCodigo(leerLinea("  Código: "));
            p.setNombre(leerLinea("  Nombre: "));
            p.setDescripcion(leerLinea("  Descripción: "));
            p.setCategoriaId(leerEntero("  ID Categoría (1-Camisas,2-Pantalones,3-Vestidos,4-Abrigos,5-Accesorios): "));
            p.setTalla(Producto.Talla.valueOf(leerLinea("  Talla (XS/S/M/L/XL/XXL/UNICA): ").toUpperCase()));
            p.setColor(leerLinea("  Color: "));
            p.setPrecioUnitario(new BigDecimal(leerLinea("  Precio unitario: ")));
            p.setStockActual(leerEntero("  Stock inicial: "));
            p.setStockMinimo(leerEntero("  Stock mínimo: "));
            p.setActivo(true);
            if (srv.crearProducto(sesion, p)) ok("Producto creado exitosamente.");
            else error("No se pudo crear el producto.");
        } catch (Exception e) { error("Error: " + e.getMessage()); }
        pausar();
    }

    private void editarProducto() {
        titulo("EDITAR PRODUCTO");
        try {
            String codigo = leerLinea("  Código del producto a editar: ");
            Producto p = srv.buscarProducto(sesion, codigo);
            if (p == null) { error("Producto no encontrado."); pausar(); return; }
            info("Producto actual: " + p);
            System.out.println("  (Deja vacío para no modificar el campo)");
            String nombre = leerLinea("  Nuevo nombre [" + p.getNombre() + "]: ");
            if (!nombre.isEmpty()) p.setNombre(nombre);
            String desc = leerLinea("  Nueva descripción: ");
            if (!desc.isEmpty()) p.setDescripcion(desc);
            String precioStr = leerLinea("  Nuevo precio [" + p.getPrecioUnitario() + "]: ");
            if (!precioStr.isEmpty()) p.setPrecioUnitario(new BigDecimal(precioStr));
            String stockMinStr = leerLinea("  Nuevo stock mínimo [" + p.getStockMinimo() + "]: ");
            if (!stockMinStr.isEmpty()) p.setStockMinimo(Integer.parseInt(stockMinStr));
            String activoStr = leerLinea("  ¿Activo? (s/n) [" + (p.isActivo() ? "s" : "n") + "]: ");
            if (!activoStr.isEmpty()) p.setActivo(activoStr.equalsIgnoreCase("s"));

            if (srv.actualizarProducto(sesion, p)) ok("Producto actualizado.");
            else error("No se pudo actualizar.");
        } catch (Exception e) { error("Error: " + e.getMessage()); }
        pausar();
    }

    // ── Solicitudes ───────────────────────────────────────────
    private void menuAprobarSolicitudes() {
        titulo("SOLICITUDES PENDIENTES");
        try {
            List<Solicitud> lista = srv.solicitudesPendientes(sesion);
            if (lista.isEmpty()) { info("No hay solicitudes pendientes."); pausar(); return; }
            for (Solicitud s : lista) {
                System.out.printf("  [%d] %s | Producto: %s | Cantidad: %d | Motivo: %s%n",
                        s.getId(), s.getTipo(), s.getProductoNombre(), s.getCantidad(), s.getMotivo());
            }
            separador();
            int id = leerEntero("  ID de solicitud a resolver (0=cancelar): ");
            if (id == 0) return;

            System.out.println("  1. Aprobar   2. Rechazar");
            int dec = leerOpcion(1, 2);
            String obs = leerLinea("  Observación (opcional): ");
            Solicitud.Estado estado = dec == 1 ? Solicitud.Estado.APROBADA : Solicitud.Estado.RECHAZADA;
            String resultado = srv.resolverSolicitud(sesion, id, estado, obs);
            ok(resultado);
        } catch (Exception e) { error("Error: " + e.getMessage()); }
        pausar();
    }

    private void verTodasSolicitudes() {
        titulo("HISTORIAL DE SOLICITUDES");
        try {
            List<Solicitud> lista = srv.todasLasSolicitudes(sesion);
            if (lista.isEmpty()) { info("Sin solicitudes."); pausar(); return; }
            for (Solicitud s : lista) {
                String color = switch (s.getEstado()) {
                    case APROBADA  -> VERDE;
                    case RECHAZADA -> ROJO;
                    default        -> AMARILLO;
                };
                System.out.printf("  %s[%d]%s %s | %s x%d | %s%n",
                        color, s.getId(), RESET,
                        s.getCreadoEn().toLocalDate(),
                        s.getTipo(), s.getCantidad(),
                        s.getProductoNombre());
            }
        } catch (SQLException e) { error("Error DB: " + e.getMessage()); }
        pausar();
    }

    // ── Despachos ─────────────────────────────────────────────
    private void verTodosDespachos() {
        titulo("HISTORIAL DE DESPACHOS");
        try {
            List<Despacho> lista = srv.todosLosDespachos(sesion);
            if (lista.isEmpty()) { info("Sin despachos."); pausar(); return; }
            for (Despacho d : lista) {
                String col = d.getEstado() == Despacho.Estado.DESPACHADO ? VERDE : ROJO;
                System.out.printf("  %s%s%s | %s x%d → %s%n",
                        col, d.getFolio(), RESET, d.getProductoNombre(),
                        d.getCantidad(), d.getDestinatario());
            }
        } catch (SQLException e) { error("Error DB: " + e.getMessage()); }
        pausar();
    }

    // ── Usuarios ──────────────────────────────────────────────
    private void menuUsuarios() {
        titulo("GESTIÓN DE USUARIOS");
        System.out.println("  1. Listar usuarios");
        System.out.println("  2. Crear usuario");
        System.out.println("  3. Cambiar contraseña");
        System.out.println("  0. Regresar");
        separador();
        switch (leerOpcion(0, 3)) {
            case 1 -> listarUsuarios();
            case 2 -> crearUsuario();
            case 3 -> cambiarContrasena();
        }
    }

    private void listarUsuarios() {
        titulo("USUARIOS DEL SISTEMA");
        try {
            for (Usuario u : srv.listarUsuarios(sesion)) {
                String estado = u.isActivo() ? VERDE + "ACTIVO" + RESET : ROJO + "INACTIVO" + RESET;
                System.out.printf("  [%d] %-20s %-15s %-15s %s%n",
                        u.getId(), u.getNombre(), u.getUsuario(), u.getRol(), estado);
            }
        } catch (SQLException e) { error("Error DB: " + e.getMessage()); }
        pausar();
    }

    private void crearUsuario() {
        titulo("CREAR USUARIO");
        try {
            Usuario u = new Usuario();
            u.setNombre(leerLinea("  Nombre completo: "));
            u.setUsuario(leerLinea("  Nombre de usuario: "));
            u.setContrasena(leerLinea("  Contraseña: "));
            System.out.println("  Roles: 1-ADMINISTRADOR  2-ALMACENISTA  3-DESPACHADOR");
            int rol = leerOpcion(1, 3);
            u.setRol(Usuario.Rol.values()[rol - 1]);
            u.setActivo(true);
            if (srv.crearUsuario(sesion, u)) ok("Usuario creado exitosamente.");
            else error("No se pudo crear el usuario.");
        } catch (Exception e) { error("Error: " + e.getMessage()); }
        pausar();
    }

    private void cambiarContrasena() {
        titulo("CAMBIAR CONTRASEÑA");
        try {
            int userId = leerEntero("  ID del usuario: ");
            String nuevaPass = leerLinea("  Nueva contraseña: ");
            if (srv.cambiarContrasena(sesion, userId, nuevaPass)) ok("Contraseña actualizada.");
            else error("No se pudo actualizar.");
        } catch (Exception e) { error("Error: " + e.getMessage()); }
        pausar();
    }

    // ── Alertas ───────────────────────────────────────────────
    private void alertasStockBajo() {
        titulo("ALERTAS DE STOCK BAJO");
        try {
            List<Producto> lista = srv.consultarStockBajo(sesion);
            if (lista.isEmpty()) { ok("Todos los productos tienen stock suficiente."); pausar(); return; }
            System.out.printf("  %-8s %-25s %-8s %-8s%n", "Código","Nombre","Stock","Mínimo");
            separador();
            for (Producto p : lista) {
                System.out.printf(ROJO + "  %-8s %-25s %-8d %-8d%n" + RESET,
                        p.getCodigo(), p.getNombre(), p.getStockActual(), p.getStockMinimo());
            }
        } catch (SQLException e) { error("Error DB: " + e.getMessage()); }
        pausar();
    }
}
