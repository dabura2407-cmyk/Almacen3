package almacen.dao;

import almacen.model.Usuario;
import almacen.util.ConexionDB;
import almacen.util.Hasher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos para la tabla `usuarios`. */
public class UsuarioDAO {

    // ── Autenticación ─────────────────────────────────────────
    public Usuario autenticar(String usuario, String contrasena) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE usuario = ? AND contrasena = ? AND activo = 1";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, Hasher.sha256(contrasena));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    // ── CRUD ──────────────────────────────────────────────────
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY rol, nombre";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public boolean insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, usuario, contrasena, rol) VALUES (?,?,?,?)";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsuario());
            ps.setString(3, Hasher.sha256(u.getContrasena()));
            ps.setString(4, u.getRol().name());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET nombre=?, rol=?, activo=? WHERE id=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRol().name());
            ps.setBoolean(3, u.isActivo());
            ps.setInt(4, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cambiarContrasena(int userId, String nuevaContrasena) throws SQLException {
        String sql = "UPDATE usuarios SET contrasena=? WHERE id=?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, Hasher.sha256(nuevaContrasena));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Mapeo ─────────────────────────────────────────────────
    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("usuario"),
                rs.getString("contrasena"),
                Usuario.Rol.valueOf(rs.getString("rol")),
                rs.getBoolean("activo")
        );
    }
}
