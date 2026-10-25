import com.sun.net.httpserver.*;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;

/** Adaptación web de la aplicación Swing original. Conserva SQLite, JDBC y BaseDatos. */
public class WebMain {
    private static final Gson JSON = new Gson();
    private static final BaseDatos DAO = new BaseDatos();
    private static final int MAX_BODY = 16_384;

    public static void main(String[] args) throws Exception {
        BaseDatos.inicializar();
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/api/productos", WebMain::products);
        server.createContext("/api/aleatorios", WebMain::randomProducts);
        server.createContext("/", WebMain::staticFiles);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("Sistema web iniciado en http://localhost:" + port);
    }
    @FunctionalInterface interface Work { Object execute() throws Exception; }
    private static void execute(HttpExchange x, Work work) throws IOException {
        try { sendJson(x, 200, Map.of("ok", true, "data", work.execute())); }
        catch (IllegalArgumentException | JsonParseException e) { sendJson(x, 400, Map.of("ok",false,"error",e.getMessage()==null?"Datos inválidos":e.getMessage())); }
        catch (SQLException e) { 
            int status = e.getMessage()!=null && e.getMessage().contains("UNIQUE constraint") ? 409 : 500;
            sendJson(x, status, Map.of("ok",false,"error",status==409?"Ese código ya existe.":"Error de base de datos."));
            if(status==500) e.printStackTrace();
        }
        catch(Exception e){ e.printStackTrace(); sendJson(x,500,Map.of("ok",false,"error","Error interno.")); }
    }
    private static void products(HttpExchange x) throws IOException {
        String method=x.getRequestMethod();
        String path=x.getRequestURI().getPath();
        String base="/api/productos";
        String code=path.equals(base)||path.equals(base+"/")?null: path.startsWith(base+"/")?URLDecoder.decode(path.substring(base.length()+1),StandardCharsets.UTF_8):null;
        execute(x, () -> {
            if (method.equals("GET") && code==null) return DAO.listar(query(x.getRequestURI().getRawQuery(),"q"));
            if (method.equals("POST") && code==null) {
                JsonObject p=body(x);
                String codigo=required(p,"codigo"), nombre=required(p,"nombre");
                DAO.insertar(new Producto(codigo,nombre,optional(p,"descripcion"),price(p),quantity(p)));
                return "Producto registrado correctamente.";
            }
            if (method.equals("PUT") && code!=null && !code.isBlank()) {
                JsonObject p=body(x);
                if(!DAO.actualizar(code,optional(p,"descripcion"),price(p),quantity(p))) throw new IllegalArgumentException("Producto no encontrado.");
                return "Producto actualizado correctamente.";
            }
            if (method.equals("DELETE") && code!=null && !code.isBlank()) {
                if(!DAO.eliminar(code)) throw new IllegalArgumentException("Producto no encontrado.");
                return "Producto eliminado correctamente.";
            }
            throw new IllegalArgumentException("Operación no disponible.");
        });
    }
    private static void randomProducts(HttpExchange x) throws IOException {
        execute(x,()-> {if(!"POST".equals(x.getRequestMethod())) throw new IllegalArgumentException("Método no válido."); return "Se agregaron " + DAO.generarDiezAleatorios() + " productos.";});
    }
    private static void staticFiles(HttpExchange x) throws IOException {
        if(!"GET".equals(x.getRequestMethod())) {x.sendResponseHeaders(405,-1);x.close();return;}
        String path=x.getRequestURI().getPath();
        if(!path.equals("/") && !path.equals("/index.html")) {x.sendResponseHeaders(404,-1);x.close();return;}
        try(InputStream in=WebMain.class.getResourceAsStream("/public/index.html")) {
            if(in==null){x.sendResponseHeaders(404,-1);x.close();return;}
            byte[] data=in.readAllBytes();
            x.getResponseHeaders().set("Content-Type","text/html; charset=utf-8");
            x.getResponseHeaders().set("Cache-Control","no-store");
            x.sendResponseHeaders(200,data.length);
            try(OutputStream out=x.getResponseBody()){out.write(data);}
        }
    }
    private static JsonObject body(HttpExchange x) throws IOException {
        if(x.getRequestHeaders().getFirst("Content-Length")!=null){
            try { if(Long.parseLong(x.getRequestHeaders().getFirst("Content-Length"))>MAX_BODY)throw new IllegalArgumentException("Solicitud demasiado grande."); }
            catch(NumberFormatException e){throw new IllegalArgumentException("Longitud inválida.");}
        }
        byte[] bytes=x.getRequestBody().readNBytes(MAX_BODY+1);
        if(bytes.length>MAX_BODY)throw new IllegalArgumentException("Solicitud demasiado grande.");
        try{return JSON.fromJson(new String(bytes,StandardCharsets.UTF_8),JsonObject.class);}catch(Exception e){throw new IllegalArgumentException("JSON inválido.");}
    }
    private static String required(JsonObject p,String field){String v=optional(p,field);if(v.isBlank())throw new IllegalArgumentException("El campo "+field+" es obligatorio.");if(v.length()>100)throw new IllegalArgumentException("Campo demasiado largo: "+field);return v;}
    private static String optional(JsonObject p,String field){return p!=null && p.has(field)&&!p.get(field).isJsonNull()?p.get(field).getAsString().trim():"";}
    private static BigDecimal price(JsonObject p){try{BigDecimal v=new BigDecimal(optional(p,"precio"));if(v.signum()<0)throw new Exception();return v;}catch(Exception e){throw new IllegalArgumentException("El precio debe ser un número no negativo.");}}
    private static int quantity(JsonObject p){try{int n=Integer.parseInt(optional(p,"cantidad"));if(n<0)throw new Exception();return n;}catch(Exception e){throw new IllegalArgumentException("La cantidad debe ser un entero no negativo.");}}
    private static String query(String raw,String key){if(raw==null)return "";for(String v:raw.split("&")){String[] pair=v.split("=",2);if(pair.length==2&&pair[0].equals(key))return URLDecoder.decode(pair[1],StandardCharsets.UTF_8);}return "";}
    private static void sendJson(HttpExchange x,int status,Object obj) throws IOException {
        byte[] b=JSON.toJson(obj).getBytes(StandardCharsets.UTF_8);
        x.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
        x.getResponseHeaders().set("Cache-Control","no-store");
        x.getResponseHeaders().set("X-Content-Type-Options","nosniff");
        x.sendResponseHeaders(status,b.length);try(OutputStream out=x.getResponseBody()){out.write(b);}
    }
}
