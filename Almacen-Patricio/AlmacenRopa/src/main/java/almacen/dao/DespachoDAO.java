package almacen.dao;

import almacen.model.Despacho;
import almacen.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos para la tabla `despachos`. */
public class DespachoDAO {

    private static final String BASE_SQL = """
        SELECT d.*,
               p.nombre  AS prod_nombre,
               u.nombre  AS desp_nombre
        FROM despachos d
        JOIN productos p ON p.id = d.producto_id
        JOIN usuarios  u ON u.id = d.despachador_id
        """;

    public List<Despacho> listarTodos() throws SQLException {
        List<Despacho> lista = new ArrayList<>();
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(BASE_SQL + " ORDER BY d.creado_en DESC")) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Despacho> listarPorDespachador(int userId) throws SQLException {
        List<Despacho> lista = new ArrayList<>();
        String sql = BASE_SQL + " WHERE d.despachador_id = ? ORDER BY d.creado_en DESC";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public int insertar(Despacho d) throws SQLException {
        String sql = """
            INSERT INTO despachos
              (folio, producto_id, cantidad, destinatario, estado, despachador_id, observacion)
            VALUES (?,?,?,?,?,?,?)""";
        try (PreparedStatement ps = ConexionDB.getConexion()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getFolio());
            ps.setInt(2, d.getProductoId());
            ps.setInt(3, d.getCantidad());
            ps.setString(4, d.getDestinatario());
            ps.setString(5, d.getEstado().name());
            ps.setInt(6, d.getDespachadorId());
            ps.setString(7, d.getObservacion());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private Despacho mapear(ResultSet rs) throws SQLException {
        Despacho d = new Despacho();
        d.setId(rs.getInt("id"));
        d.setFolio(rs.getString("folio"));
        d.setProductoId(rs.getInt("producto_id"));
        d.setProductoNombre(rs.getString("prod_nombre"));
        d.setCantidad(rs.getInt("cantidad"));
        d.setDestinatario(rs.getString("destinatario"));
        d.setEstado(Despacho.Estado.valueOf(rs.getString("estado")));
        d.setDespachadorId(rs.getInt("despachador_id"));
        d.setDespachadorNombre(rs.getString("desp_nombre"));
        d.setObservacion(rs.getString("observacion"));
        Timestamp ts = rs.getTimestamp("creado_en");
        if (ts != null) d.setCreadoEn(ts.toLocalDateTime());
        return d;
    }
}
