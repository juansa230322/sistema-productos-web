import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a SQLite con JDBC y operaciones CRUD. */
public class BaseDatos {
    private static final String URL;
    static {
        String ruta = System.getenv().getOrDefault("DB_PATH", "productos.db");
        java.io.File archivo = new java.io.File(ruta);
        java.io.File carpeta = archivo.getAbsoluteFile().getParentFile();
        if (carpeta != null) carpeta.mkdirs();
        URL = "jdbc:sqlite:" + archivo.getAbsolutePath();
    }
    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    public static void inicializar() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS productos (" +
            "codigo TEXT PRIMARY KEY NOT NULL, " +
            "nombre TEXT NOT NULL CHECK(length(trim(nombre)) > 0), " +
            "descripcion TEXT NOT NULL DEFAULT '', " +
            "precio NUMERIC NOT NULL CHECK(precio >= 0), " +
            "cantidad INTEGER NOT NULL CHECK(cantidad >= 0))";
        try (Connection c = obtener(); Statement st = c.createStatement()) {
            st.execute(sql);
        }
    }
    public void insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO productos(codigo,nombre,descripcion,precio,cantidad) VALUES(?,?,?,?,?)";
        try (Connection c = obtener(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,p.getCodigo()); ps.setString(2,p.getNombre());
            ps.setString(3,p.getDescripcion()); ps.setBigDecimal(4,p.getPrecio());
            ps.setInt(5,p.getCantidad()); ps.executeUpdate();
        }
    }
    public List<Producto> listar(String filtro) throws SQLException {
        String sql = "SELECT codigo,nombre,descripcion,precio,cantidad FROM productos " +
                     "WHERE codigo LIKE ? OR nombre LIKE ? ORDER BY nombre,codigo";
        List<Producto> resultado = new ArrayList<>();
        try (Connection c = obtener(); PreparedStatement ps = c.prepareStatement(sql)) {
            String patron = "%" + filtro + "%";
            ps.setString(1,patron); ps.setString(2,patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.add(new Producto(rs.getString("codigo"),
                    rs.getString("nombre"),rs.getString("descripcion"),
                    rs.getBigDecimal("precio"),rs.getInt("cantidad")));
            }
        }
        return resultado;
    }
    public Producto buscarCodigo(String codigo) throws SQLException {
        String sql = "SELECT codigo,nombre,descripcion,precio,cantidad FROM productos WHERE codigo=?";
        try (Connection c = obtener(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Producto(rs.getString("codigo"),rs.getString("nombre"),
                    rs.getString("descripcion"),rs.getBigDecimal("precio"),rs.getInt("cantidad"));
            }
        }
        return null;
    }
    public boolean actualizar(String codigo, String descripcion, java.math.BigDecimal precio, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET descripcion=?,precio=?,cantidad=? WHERE codigo=?";
        try (Connection c = obtener(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,descripcion); ps.setBigDecimal(2,precio);
            ps.setInt(3,cantidad); ps.setString(4,codigo);
            return ps.executeUpdate() > 0;
        }
    }
    public boolean eliminar(String codigo) throws SQLException {
        try (Connection c = obtener(); PreparedStatement ps = c.prepareStatement("DELETE FROM productos WHERE codigo=?")) {
            ps.setString(1,codigo); return ps.executeUpdate() > 0;
        }
    }

    /** Inserta exactamente diez productos nuevos, sin reemplazar los existentes. */
    public int generarDiezAleatorios() throws SQLException {
        String[] nombres = {"Arroz", "Leche", "Pan", "Cafe", "Azucar", "Aceite", "Pasta", "Galletas", "Jabon", "Harina"};
        java.util.Random random = new java.util.Random();
        int insertados = 0;
        String sql = "INSERT INTO productos(codigo,nombre,descripcion,precio,cantidad) VALUES(?,?,?,?,?)";
        try (Connection c = obtener(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < 10; i++) {
                String codigo = "AUTO-" + java.util.UUID.randomUUID().toString().substring(0, 8);
                ps.setString(1, codigo);
                ps.setString(2, nombres[i]);
                ps.setString(3, "Producto de prueba generado automaticamente");
                ps.setInt(4, 1000 + random.nextInt(99001));
                ps.setInt(5, random.nextInt(101));
                insertados += ps.executeUpdate();
            }
        }
        return insertados;
    }
}
