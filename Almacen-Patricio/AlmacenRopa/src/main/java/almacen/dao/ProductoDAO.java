package almacen.dao;

import almacen.model.Producto;
import almacen.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos para la tabla `productos`. */
public class ProductoDAO {

    // ── Consultas ─────────────────────────────────────────────
    public List<Producto> listarActivos() throws SQLException {
        return listar("WHERE p.activo = 1 ORDER BY c.nombre, p.nombre");
    }

    public List<Producto> listarTodos() throws SQLException {
        return listar("ORDER BY c.nombre, p.nombre");
    }

    public List<Producto> listarStockBajo() throws SQLException {
        return listar("WHERE p.activo = 1 AND p.stock_actual < p.stock_minimo ORDER BY p.nombre");
    }

    public Producto buscarPorId(int id) throws SQLException {
        List<Producto> res = listar("WHERE p.id = ?", id);
        return res.isEmpty() ? null : res.get(0);
    }

    public Producto buscarPorCodigo(String codigo) throws SQLException {
        List<Producto> res = listar("WHERE p.codigo = ?", codigo);
        return res.isEmpty() ? null : res.get(0);
    }

    public List<Producto> buscarPorNombre(String texto) throws SQLException {
        String sql = BASE_SQL + " WHERE p.activo=1 AND p.nombre LIKE ? ORDER BY p.nombre";
        List<Producto> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ── CRUD ──────────────────────────────────────────────────
    public boolean insertar(Producto p) throws SQLException {
        String sql = """
            INSERT INTO productos
              (codigo,nombre,descripcion,categoria_id,talla,color,precio_unitario,stock_actual,stock_minimo)
            VALUES (?,?,?,?,?,?,?,?,?)""";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDescripcion());
            ps.setInt(4, p.getCategoriaId());
            ps.setString(5, p.getTalla().name());
            ps.setString(6, p.getColor());
            ps.setBigDecimal(7, p.getPrecioUnitario());
            ps.setInt(8, p.getStockActual());
            ps.setInt(9, p.getStockMinimo());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Producto p) throws SQLException {
        String sql = """
            UPDATE productos SET
              nombre=?, descripcion=?, categoria_id=?, talla=?, color=?,
              precio_unitario=?, stock_minimo=?, activo=?
            WHERE id=?""";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setInt(3, p.getCategoriaId());
            ps.setString(4, p.getTalla().name());
            ps.setString(5, p.getColor());
            ps.setBigDecimal(6, p.getPrecioUnitario());
            ps.setInt(7, p.getStockMinimo());
            ps.setBoolean(8, p.isActivo());
            ps.setInt(9, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Actualiza únicamente el stock (usado internamente tras aprobar solicitud/despacho). */
    public boolean actualizarStock(int productoId, int nuevoStock) throws SQLException {
        String sql = "UPDATE productos SET stock_actual=? WHERE id=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setInt(2, productoId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Descuenta stock de forma atómica: solo aplica si stock_actual >= cantidad.
     * Evita condiciones de carrera entre la lectura y la escritura del stock.
     * Devuelve false si no había stock suficiente en el momento del UPDATE.
     */
    public boolean descontarStockSiAlcanza(int productoId, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET stock_actual = stock_actual - ? " +
                     "WHERE id=? AND stock_actual >= ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, productoId);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() > 0;
        }
    }

    /** Incrementa stock de forma atómica (usado en altas aprobadas). */
    public boolean incrementarStock(int productoId, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET stock_actual = stock_actual + ? WHERE id=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, productoId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Helpers privados ──────────────────────────────────────
    private static final String BASE_SQL =
            "SELECT p.*, c.nombre AS cat_nombre FROM productos p " +
            "JOIN categorias c ON c.id = p.categoria_id ";

    private List<Producto> listar(String where, Object... params) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = BASE_SQL + where;
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Integer) ps.setInt(i + 1, (Integer) params[i]);
                else                              ps.setString(i + 1, params[i].toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setCategoriaId(rs.getInt("categoria_id"));
        p.setCategoriaNombre(rs.getString("cat_nombre"));
        p.setTalla(Producto.Talla.valueOf(rs.getString("talla")));
        p.setColor(rs.getString("color"));
        p.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        p.setStockActual(rs.getInt("stock_actual"));
        p.setStockMinimo(rs.getInt("stock_minimo"));
        p.setActivo(rs.getBoolean("activo"));
        Timestamp ts = rs.getTimestamp("creado_en");
        if (ts != null) p.setCreadoEn(ts.toLocalDateTime());
        return p;
    }
}
