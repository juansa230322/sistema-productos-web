FROM gradle:8.14-jdk21 AS builder
WORKDIR /workspace
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle src ./src
RUN gradle installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/build/install/SistemaProductosJavaWeb/ /app/
ENV PORT=8080 DB_PATH=/data/productos.db
EXPOSE 8080
CMD ["/app/bin/SistemaProductosJavaWeb"]
