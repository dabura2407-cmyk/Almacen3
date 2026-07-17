package almacen.model;

import java.time.LocalDateTime;

/** Representa un despacho de mercancía. */
public class Despacho {

    public enum Estado { DESPACHADO, SIN_STOCK }

    private int           id;
    private String        folio;
    private int           productoId;
    private String        productoNombre;
    private int           cantidad;
    private String        destinatario;
    private Estado        estado;
    private int           despachadorId;
    private String        despachadorNombre;
    private String        observacion;
    private LocalDateTime creadoEn;

    public Despacho() {}

    // ── Getters / Setters ──────────────────────────────────────
    public int    getId()                           { return id; }
    public void   setId(int id)                     { this.id = id; }

    public String getFolio()                        { return folio; }
    public void   setFolio(String f)                { this.folio = f; }

    public int    getProductoId()                   { return productoId; }
    public void   setProductoId(int p)              { this.productoId = p; }

    public String getProductoNombre()               { return productoNombre; }
    public void   setProductoNombre(String pn)      { this.productoNombre = pn; }

    public int    getCantidad()                     { return cantidad; }
    public void   setCantidad(int c)                { this.cantidad = c; }

    public String getDestinatario()                 { return destinatario; }
    public void   setDestinatario(String d)         { this.destinatario = d; }

    public Estado getEstado()                       { return estado; }
    public void   setEstado(Estado e)               { this.estado = e; }

    public int    getDespachadorId()                { return despachadorId; }
    public void   setDespachadorId(int d)           { this.despachadorId = d; }

    public String getDespachadorNombre()            { return despachadorNombre; }
    public void   setDespachadorNombre(String dn)   { this.despachadorNombre = dn; }

    public String getObservacion()                  { return observacion; }
    public void   setObservacion(String o)          { this.observacion = o; }

    public LocalDateTime getCreadoEn()              { return creadoEn; }
    public void          setCreadoEn(LocalDateTime l) { this.creadoEn = l; }

    @Override
    public String toString() {
        return String.format("[%s] %s x%d → %s | %s", folio, productoNombre, cantidad, destinatario, estado);
    }
}
