package almacen.model;

/** Representa un usuario del sistema. */
public class Usuario {

    public enum Rol { ADMINISTRADOR, ALMACENISTA, DESPACHADOR }

    private int    id;
    private String nombre;
    private String usuario;
    private String contrasena;  // almacenada como hash SHA-256
    private Rol    rol;
    private boolean activo;

    public Usuario() {}

    public Usuario(int id, String nombre, String usuario, String contrasena, Rol rol, boolean activo) {
        this.id         = id;
        this.nombre     = nombre;
        this.usuario    = usuario;
        this.contrasena = contrasena;
        this.rol        = rol;
        this.activo     = activo;
    }

    // ── Getters / Setters ──────────────────────────────────────
    public int     getId()          { return id; }
    public void    setId(int id)    { this.id = id; }

    public String  getNombre()              { return nombre; }
    public void    setNombre(String n)      { this.nombre = n; }

    public String  getUsuario()             { return usuario; }
    public void    setUsuario(String u)     { this.usuario = u; }

    public String  getContrasena()          { return contrasena; }
    public void    setContrasena(String c)  { this.contrasena = c; }

    public Rol     getRol()                 { return rol; }
    public void    setRol(Rol r)            { this.rol = r; }

    public boolean isActivo()               { return activo; }
    public void    setActivo(boolean a)     { this.activo = a; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - %s", id, nombre, usuario, rol);
    }
}
