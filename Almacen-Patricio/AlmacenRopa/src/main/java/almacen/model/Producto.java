package almacen.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Representa una prenda de ropa en el almacén. */
public class Producto {

    public enum Talla { XS, S, M, L, XL, XXL, UNICA }

    private int          id;
    private String       codigo;
    private String       nombre;
    private String       descripcion;
    private int          categoriaId;
    private String       categoriaNombre;
    private Talla        talla;
    private String       color;
    private BigDecimal   precioUnitario;
    private int          stockActual;
    private int          stockMinimo;
    private boolean      activo;
    private LocalDateTime creadoEn;

    public Producto() {}

    // ── Getters / Setters ──────────────────────────────────────
    public int          getId()                     { return id; }
    public void         setId(int id)               { this.id = id; }

    public String       getCodigo()                 { return codigo; }
    public void         setCodigo(String c)         { this.codigo = c; }

    public String       getNombre()                 { return nombre; }
    public void         setNombre(String n)         { this.nombre = n; }

    public String       getDescripcion()            { return descripcion; }
    public void         setDescripcion(String d)    { this.descripcion = d; }

    public int          getCategoriaId()            { return categoriaId; }
    public void         setCategoriaId(int cId)     { this.categoriaId = cId; }

    public String       getCategoriaNombre()                    { return categoriaNombre; }
    public void         setCategoriaNombre(String cn)           { this.categoriaNombre = cn; }

    public Talla        getTalla()                  { return talla; }
    public void         setTalla(Talla t)           { this.talla = t; }

    public String       getColor()                  { return color; }
    public void         setColor(String c)          { this.color = c; }

    public BigDecimal   getPrecioUnitario()                 { return precioUnitario; }
    public void         setPrecioUnitario(BigDecimal p)     { this.precioUnitario = p; }

    public int          getStockActual()                    { return stockActual; }
    public void         setStockActual(int s)               { this.stockActual = s; }

    public int          getStockMinimo()                    { return stockMinimo; }
    public void         setStockMinimo(int s)               { this.stockMinimo = s; }

    public boolean      isActivo()                  { return activo; }
    public void         setActivo(boolean a)        { this.activo = a; }

    public LocalDateTime getCreadoEn()              { return creadoEn; }
    public void          setCreadoEn(LocalDateTime l) { this.creadoEn = l; }

    /** Devuelve true si el stock actual está por debajo del mínimo. */
    public boolean stockBajo() { return stockActual < stockMinimo; }

    @Override
    public String toString() {
        return String.format("[%s] %s | Talla: %s | Color: %s | Stock: %d | $%.2f",
                codigo, nombre, talla, color, stockActual, precioUnitario);
    }
}
