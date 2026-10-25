

import java.math.BigDecimal;

public class Producto {
    private String codigo, nombre, descripcion;
    private BigDecimal precio;
    private int cantidad;

    public Producto(String codigo, String nombre, String descripcion, BigDecimal precio, int cantidad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
    }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
}
