# Etapa de construcción
FROM ubuntu:latest AS build

# Instala Maven y JDK 21 en la imagen base
RUN apt-get update && apt-get install -y openjdk-21-jdk maven

# Crea un directorio para la aplicación
WORKDIR /app

# Copia los archivos de la aplicación al directorio de trabajo
COPY . .

# Compila y empaqueta la aplicación usando Maven
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM openjdk:21-jdk-slim

# Instalar curl para healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Exponer el puerto 8888 para la API
EXPOSE 8888

# Copia el archivo JAR generado desde la etapa de construcción
COPY --from=build /app/target/cooperApp-cloud-0.0.1.jar config-server.jar

# Define el comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "config-server.jar"]