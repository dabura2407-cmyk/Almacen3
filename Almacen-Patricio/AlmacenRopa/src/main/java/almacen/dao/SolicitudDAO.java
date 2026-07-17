package almacen.dao;

import almacen.model.Solicitud;
import almacen.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos para la tabla `solicitudes`. */
public class SolicitudDAO {

    private static final String BASE_SQL = """
        SELECT s.*,
               p.nombre  AS prod_nombre,
               us.nombre AS solic_nombre,
               ua.nombre AS apro_nombre
        FROM solicitudes s
        JOIN productos p  ON p.id  = s.producto_id
        JOIN usuarios  us ON us.id = s.solicitante_id
        LEFT JOIN usuarios ua ON ua.id = s.aprobador_id
        """;

    // ── Consultas ─────────────────────────────────────────────
    public List<Solicitud> listarPendientes() throws SQLException {
        return listar(BASE_SQL + " WHERE s.estado = 'PENDIENTE' ORDER BY s.creado_en");
    }

    public List<Solicitud> listarPorSolicitante(int userId) throws SQLException {
        return listarParam(BASE_SQL + " WHERE s.solicitante_id = ? ORDER BY s.creado_en DESC", userId);
    }

    public List<Solicitud> listarTodas() throws SQLException {
        return listar(BASE_SQL + " ORDER BY s.creado_en DESC");
    }

    public Solicitud buscarPorId(int id) throws SQLException {
        List<Solicitud> res = listarParam(BASE_SQL + " WHERE s.id = ?", id);
        return res.isEmpty() ? null : res.get(0);
    }

    // ── Insertar ──────────────────────────────────────────────
    public int insertar(Solicitud s) throws SQLException {
        String sql = """
            INSERT INTO solicitudes (tipo, producto_id, cantidad, motivo, solicitante_id)
            VALUES (?,?,?,?,?)""";
        try (PreparedStatement ps = ConexionDB.getConexion()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getTipo().name());
            ps.setInt(2, s.getProductoId());
            ps.setInt(3, s.getCantidad());
            ps.setString(4, s.getMotivo());
            ps.setInt(5, s.getSolicitanteId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // ── Resolver (aprobar / rechazar) ─────────────────────────
    public boolean resolver(int solicitudId, Solicitud.Estado estado,
                             int aprobadorId, String observacion) throws SQLException {
        String sql = """
            UPDATE solicitudes
               SET estado=?, aprobador_id=?, observacion=?, resuelto_en=NOW()
             WHERE id=? AND estado='PENDIENTE'""";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, aprobadorId);
            ps.setString(3, observacion);
            ps.setInt(4, solicitudId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private List<Solicitud> listar(String sql) throws SQLException {
        List<Solicitud> lista = new ArrayList<>();
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private List<Solicitud> listarParam(String sql, int param) throws SQLException {
        List<Solicitud> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Solicitud mapear(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.setId(rs.getInt("id"));
        s.setTipo(Solicitud.Tipo.valueOf(rs.getString("tipo")));
        s.setProductoId(rs.getInt("producto_id"));
        s.setProductoNombre(rs.getString("prod_nombre"));
        s.setCantidad(rs.getInt("cantidad"));
        s.setMotivo(rs.getString("motivo"));
        s.setEstado(Solicitud.Estado.valueOf(rs.getString("estado")));
        s.setSolicitanteId(rs.getInt("solicitante_id"));
        s.setSolicitanteNombre(rs.getString("solic_nombre"));
        int aprobId = rs.getInt("aprobador_id");
        if (!rs.wasNull()) {
            s.setAprobadorId(aprobId);
            s.setAprobadorNombre(rs.getString("apro_nombre"));
        }
        s.setObservacion(rs.getString("observacion"));
        Timestamp c = rs.getTimestamp("creado_en");
        if (c != null) s.setCreadoEn(c.toLocalDateTime());
        Timestamp r = rs.getTimestamp("resuelto_en");
        if (r != null) s.setResueltoEn(r.toLocalDateTime());
        return s;
    }
}
