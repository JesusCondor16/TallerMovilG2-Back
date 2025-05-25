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

# Exponer el puerto 8080 para la API
EXPOSE 8080

# Copia el archivo JAR generado desde la etapa de construcción
COPY --from=build /app/target/cooperApp-0.1.0.jar app.jar

# Define el comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
