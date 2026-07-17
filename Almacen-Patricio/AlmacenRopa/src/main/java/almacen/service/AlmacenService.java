package almacen.service;

import almacen.dao.*;
import almacen.model.*;
import almacen.util.ConexionDB;

import java.sql.SQLException;
import java.util.List;

/**
 * Capa de servicio: contiene la lógica de negocio del gestor de almacén.
 * Coordina DAOs y aplica reglas (permisos, flujo de aprobación, actualización de stock).
 */
public class AlmacenService {

    private final UsuarioDAO   usuarioDAO   = new UsuarioDAO();
    private final ProductoDAO  productoDAO  = new ProductoDAO();
    private final SolicitudDAO solicitudDAO = new SolicitudDAO();
    private final DespachoDAO  despachoDAO  = new DespachoDAO();
    private final HistorialDAO historialDAO = new HistorialDAO();

    // ════════════════════════════════════════════════════════
    //  AUTENTICACIÓN
    // ════════════════════════════════════════════════════════

    public Usuario login(String usuario, String contrasena) throws SQLException {
        return usuarioDAO.autenticar(usuario, contrasena);
    }

    // ════════════════════════════════════════════════════════
    //  GESTIÓN DE USUARIOS  (solo ADMINISTRADOR)
    // ════════════════════════════════════════════════════════

    public List<Usuario> listarUsuarios(Usuario sesion) throws SQLException {
        verificarRol(sesion, Usuario.Rol.ADMINISTRADOR);
        return usuarioDAO.listarTodos();
    }

    public boolean crearUsuario(Usuario sesion, Usuario nuevo) throws SQLException {
        verificarRol(sesion, Usuario.Rol.ADMINISTRADOR);
        return usuarioDAO.insertar(nuevo);
    }

    public boolean actualizarUsuario(Usuario sesion, Usuario u) throws SQLException {
        verificarRol(sesion, Usuario.Rol.ADMINISTRADOR);
        return usuarioDAO.actualizar(u);
    }

    public boolean cambiarContrasena(Usuario sesion, int userId, String nuevaPass) throws SQLException {
        // El propio usuario puede cambiarla, o el administrador
        if (sesion.getId() != userId) verificarRol(sesion, Usuario.Rol.ADMINISTRADOR);
        return usuarioDAO.cambiarContrasena(userId, nuevaPass);
    }

    // ════════════════════════════════════════════════════════
    //  GESTIÓN DE PRODUCTOS  (solo ADMINISTRADOR)
    // ════════════════════════════════════════════════════════

    public boolean crearProducto(Usuario sesion, Producto p) throws SQLException {
        verificarRol(sesion, Usuario.Rol.ADMINISTRADOR);
        return productoDAO.insertar(p);
    }

    public boolean actualizarProducto(Usuario sesion, Producto p) throws SQLException {
        verificarRol(sesion, Usuario.Rol.ADMINISTRADOR);
        return productoDAO.actualizar(p);
    }

    // ════════════════════════════════════════════════════════
    //  CONSULTA DE INVENTARIO  (todos los roles)
    // ════════════════════════════════════════════════════════

    public List<Producto> consultarInventario(Usuario sesion) throws SQLException {
        return productoDAO.listarActivos();
    }

    public List<Producto> consultarStockBajo(Usuario sesion) throws SQLException {
        return productoDAO.listarStockBajo();
    }

    public Producto buscarProducto(Usuario sesion, String codigo) throws SQLException {
        return productoDAO.buscarPorCodigo(codigo);
    }

    public List<Producto> buscarProductoPorNombre(Usuario sesion, String nombre) throws SQLException {
        return productoDAO.buscarPorNombre(nombre);
    }

    // ════════════════════════════════════════════════════════
    //  SOLICITUDES DE ALTA / BAJA  (ALMACENISTA las crea)
    // ════════════════════════════════════════════════════════

    /**
     * El almacenista crea una solicitud. Queda en estado PENDIENTE
     * hasta que el administrador la apruebe o rechace.
     */
    public int crearSolicitud(Usuario sesion, int productoId,
                               Solicitud.Tipo tipo, int cantidad, String motivo)
            throws SQLException {
        if (sesion.getRol() != Usuario.Rol.ALMACENISTA &&
            sesion.getRol() != Usuario.Rol.ADMINISTRADOR) {
            throw new SecurityException("Solo el almacenista puede crear solicitudes.");
        }
        Solicitud s = new Solicitud();
        s.setTipo(tipo);
        s.setProductoId(productoId);
        s.setCantidad(cantidad);
        s.setMotivo(motivo);
        s.setSolicitanteId(sesion.getId());
        return solicitudDAO.insertar(s);
    }

    public List<Solicitud> misSolicitudes(Usuario sesion) throws SQLException {
        return solicitudDAO.listarPorSolicitante(sesion.getId());
    }

    public List<Solicitud> solicitudesPendientes(Usuario sesion) throws SQLException {
        verificarRol(sesion, Usuario.Rol.ADMINISTRADOR);
        return solicitudDAO.listarPendientes();
    }

    public List<Solicitud> todasLasSolicitudes(Usuario sesion) throws SQLException {
        verificarRol(sesion, Usuario.Rol.ADMINISTRADOR);
        return solicitudDAO.listarTodas();
    }

    // ════════════════════════════════════════════════════════
    //  APROBACIÓN / RECHAZO DE SOLICITUDES  (ADMINISTRADOR)
    // ════════════════════════════════════════════════════════

    public String resolverSolicitud(Usuario sesion, int solicitudId,
                                     Solicitud.Estado decision, String observacion)
            throws SQLException {
        verificarRol(sesion, Usuario.Rol.ADMINISTRADOR);

        return ConexionDB.ejecutarEnTransaccion(() -> {
            Solicitud sol = solicitudDAO.buscarPorId(solicitudId);
            if (sol == null) return "Solicitud no encontrada.";
            if (sol.getEstado() != Solicitud.Estado.PENDIENTE) return "La solicitud ya fue resuelta.";

            if (decision != Solicitud.Estado.APROBADA) {
                solicitudDAO.resolver(solicitudId, decision, sesion.getId(), observacion);
                return "Solicitud RECHAZADA.";
            }

            // Si se aprueba, actualizar el stock de forma atómica
            Producto prod = productoDAO.buscarPorId(sol.getProductoId());
            int stockAnterior = prod.getStockActual();
            int stockNuevo;

            if (sol.getTipo() == Solicitud.Tipo.ALTA) {
                productoDAO.incrementarStock(prod.getId(), sol.getCantidad());
                stockNuevo = stockAnterior + sol.getCantidad();
            } else {
                boolean descontado = productoDAO.descontarStockSiAlcanza(prod.getId(), sol.getCantidad());
                if (!descontado) {
                    solicitudDAO.resolver(solicitudId, Solicitud.Estado.RECHAZADA,
                            sesion.getId(), "Stock insuficiente para la baja.");
                    return "Stock insuficiente. Solicitud rechazada automáticamente.";
                }
                stockNuevo = stockAnterior - sol.getCantidad();
            }

            solicitudDAO.resolver(solicitudId, decision, sesion.getId(), observacion);
            historialDAO.registrar(prod.getId(), sol.getTipo().name(),
                    sol.getCantidad(), stockAnterior, stockNuevo,
                    solicitudId, sesion.getId());

            return String.format("Solicitud APROBADA. Stock actualizado: %d → %d",
                    stockAnterior, stockNuevo);
        });
    }

    // ════════════════════════════════════════════════════════
    //  DESPACHOS  (DESPACHADOR)
    // ════════════════════════════════════════════════════════

    /**
     * El despachador registra un despacho.
     * Si hay stock suficiente → DESPACHADO y se descuenta el stock.
     * Si no hay stock         → SIN_STOCK (se registra la aclaración).
     */
    public Despacho realizarDespacho(Usuario sesion, int productoId,
                                      int cantidad, String destinatario,
                                      String observacion) throws SQLException {
        if (sesion.getRol() != Usuario.Rol.DESPACHADOR &&
            sesion.getRol() != Usuario.Rol.ADMINISTRADOR) {
            throw new SecurityException("Solo el despachador puede registrar despachos.");
        }

        return ConexionDB.ejecutarEnTransaccion(() -> {
            Producto prod = productoDAO.buscarPorId(productoId);
            if (prod == null) throw new IllegalArgumentException("Producto no encontrado.");

            Despacho d = new Despacho();
            d.setFolio(generarFolio());
            d.setProductoId(productoId);
            d.setCantidad(cantidad);
            d.setDestinatario(destinatario);
            d.setDespachadorId(sesion.getId());
            d.setObservacion(observacion);

            boolean descontado = productoDAO.descontarStockSiAlcanza(productoId, cantidad);
            if (descontado) {
                d.setEstado(Despacho.Estado.DESPACHADO);
                despachoDAO.insertar(d);
                int stockNuevo = prod.getStockActual() - cantidad;
                historialDAO.registrar(productoId, "DESPACHO",
                        cantidad, prod.getStockActual(), stockNuevo, null, sesion.getId());
            } else {
                d.setEstado(Despacho.Estado.SIN_STOCK);
                d.setObservacion("Sin stock suficiente. Disponible: " + prod.getStockActual()
                        + (observacion != null ? " | " + observacion : ""));
                despachoDAO.insertar(d);
            }
            return d;
        });
    }

    public List<Despacho> misDespachos(Usuario sesion) throws SQLException {
        return despachoDAO.listarPorDespachador(sesion.getId());
    }

    public List<Despacho> todosLosDespachos(Usuario sesion) throws SQLException {
        verificarRol(sesion, Usuario.Rol.ADMINISTRADOR);
        return despachoDAO.listarTodos();
    }

    // ════════════════════════════════════════════════════════
    //  UTILIDADES PRIVADAS
    // ════════════════════════════════════════════════════════

    private void verificarRol(Usuario sesion, Usuario.Rol rolRequerido) {
        if (sesion.getRol() != rolRequerido) {
            throw new SecurityException(
                    "Acceso denegado. Se requiere rol: " + rolRequerido);
        }
    }

    private String generarFolio() {
        return "DSP-" + System.currentTimeMillis();
    }
}
