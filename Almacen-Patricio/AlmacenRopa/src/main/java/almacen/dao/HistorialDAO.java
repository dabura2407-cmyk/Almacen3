package almacen.dao;

import almacen.util.ConexionDB;

import java.sql.*;

/** Registra movimientos de inventario para auditoría. */
public class HistorialDAO {

    public void registrar(int productoId, String tipoMovimiento,
                          int cantidad, int stockAnterior, int stockNuevo,
                          Integer referenciaId, int usuarioId) throws SQLException {
        String sql = """
            INSERT INTO historial_inventario
              (producto_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo, referencia_id, usuario_id)
            VALUES (?,?,?,?,?,?,?)""";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, productoId);
            ps.setString(2, tipoMovimiento);
            ps.setInt(3, cantidad);
            ps.setInt(4, stockAnterior);
            ps.setInt(5, stockNuevo);
            if (referenciaId != null) ps.setInt(6, referenciaId);
            else                      ps.setNull(6, Types.INTEGER);
            ps.setInt(7, usuarioId);
            ps.executeUpdate();
        }
    }
}
