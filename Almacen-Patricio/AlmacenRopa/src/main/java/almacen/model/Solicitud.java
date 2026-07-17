package almacen.model;

import java.time.LocalDateTime;

/** Representa una solicitud de alta o baja de inventario. */
public class Solicitud {

    public enum Tipo   { ALTA, BAJA }
    public enum Estado { PENDIENTE, APROBADA, RECHAZADA }

    private int           id;
    private Tipo          tipo;
    private int           productoId;
    private String        productoNombre;
    private int           cantidad;
    private String        motivo;
    private Estado        estado;
    private int           solicitanteId;
    private String        solicitanteNombre;
    private Integer       aprobadorId;
    private String        aprobadorNombre;
    private String        observacion;
    private LocalDateTime creadoEn;
    private LocalDateTime resueltoEn;

    public Solicitud() {}

    // ── Getters / Setters ──────────────────────────────────────
    public int    getId()                          { return id; }
    public void   setId(int id)                    { this.id = id; }

    public Tipo   getTipo()                        { return tipo; }
    public void   setTipo(Tipo t)                  { this.tipo = t; }

    public int    getProductoId()                  { return productoId; }
    public void   setProductoId(int p)             { this.productoId = p; }

    public String getProductoNombre()              { return productoNombre; }
    public void   setProductoNombre(String pn)     { this.productoNombre = pn; }

    public int    getCantidad()                    { return cantidad; }
    public void   setCantidad(int c)               { this.cantidad = c; }

    public String getMotivo()                      { return motivo; }
    public void   setMotivo(String m)              { this.motivo = m; }

    public Estado getEstado()                      { return estado; }
    public void   setEstado(Estado e)              { this.estado = e; }

    public int    getSolicitanteId()               { return solicitanteId; }
    public void   setSolicitanteId(int s)          { this.solicitanteId = s; }

    public String getSolicitanteNombre()           { return solicitanteNombre; }
    public void   setSolicitanteNombre(String sn)  { this.solicitanteNombre = sn; }

    public Integer getAprobadorId()                { return aprobadorId; }
    public void    setAprobadorId(Integer a)       { this.aprobadorId = a; }

    public String getAprobadorNombre()             { return aprobadorNombre; }
    public void   setAprobadorNombre(String an)    { this.aprobadorNombre = an; }

    public String getObservacion()                 { return observacion; }
    public void   setObservacion(String o)         { this.observacion = o; }

    public LocalDateTime getCreadoEn()             { return creadoEn; }
    public void          setCreadoEn(LocalDateTime l) { this.creadoEn = l; }

    public LocalDateTime getResueltoEn()                  { return resueltoEn; }
    public void          setResueltoEn(LocalDateTime l)   { this.resueltoEn = l; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s x%d | Estado: %s | Por: %s",
                id, tipo, productoNombre, cantidad, estado, solicitanteNombre);
    }
}
