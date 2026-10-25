# Sistema de Gestión de Productos — Versión web Java + JDBC

Adaptación **web** del proyecto académico Java Swing original. Se mantienen las clases de dominio `Producto.java` y acceso a SQLite `BaseDatos.java`, con consultas `PreparedStatement` y operaciones CRUD. La interfaz Swing se reemplaza por una página HTML que se comunica con `WebMain.java` mediante API HTTP.

## Funcionalidades

- Registrar productos, listar todos y buscar por código o nombre.
- Actualizar precio, descripción y cantidad.
- Eliminar productos con confirmación.
- Validar precio >= 0 y cantidad entera >= 0.
- Generar 10 productos aleatorios.
- Guardar datos en SQLite mediante JDBC.

## En tu computador

Requisitos: Java 21 y Gradle (o Docker).

```powershell
gradle run
```

Abre `http://localhost:8080` en el navegador. La base de datos local se crea en `productos.db` (excluida del repositorio).

Con Docker:

```bash
docker build -t productos-java-web .
docker run --rm -p 8080:8080 -e DB_PATH=/data/productos.db -v productos_java_data:/data productos-java-web
```

## Publicar en Railway para que el profesor abra solo un enlace

1. Sube **el contenido de esta carpeta** como un repositorio separado de GitHub (con `Dockerfile` en la raíz), o coloca esta carpeta en el repositorio existente y configura `Root Directory` con su ruta.
2. En [Railway](https://railway.com/), crea `New Project` → `Deploy from GitHub repo` y selecciona este proyecto.
3. Railway detectará automáticamente `Dockerfile` en la raíz. Espera que la compilación termine.
4. **Para que SQLite conserve los registros al reiniciar o volver a desplegar**, conecta un **Volume** al servicio, con ruta de montaje `/data`; en Variables deja `DB_PATH=/data/productos.db` (el Dockerfile ya lo establece por defecto). Consulta el costo y disponibilidad del volumen en tu plan.
5. En `Settings` → `Networking` → `Generate Domain`, genera una dirección pública.
6. Comparte **ese dominio de Railway** con el profesor, no el enlace del repositorio. El navegador mostrará el sistema sin instalar Java.

**Importante:** Railway puede requerir método de pago y genera cargos de alojamiento. Sin un volumen persistente, SQLite perderá datos tras determinados reinicios/despliegues. Esta es una **demo académica pública sin cuentas de usuario**: cualquiera con el enlace puede modificar o eliminar registros. No almacenes datos personales ni información sensible.

## Diferencias con la versión de escritorio

La versión Swing sigue siendo válida para el taller, y esta versión web es una adaptación para demostrarla desde un enlace. No se trata del mismo ejecutable ni de Swing corriendo dentro del navegador. En el informe y el video distingue cuál de las dos versiones estás demostrando.
