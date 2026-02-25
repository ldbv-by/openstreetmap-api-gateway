# ---- Build Stage ----
FROM maven:3.9.12-eclipse-temurin-25 AS build

WORKDIR /src
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests clean package

# ---- Runtime Stage ----
FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /src/target/*.jar ./openstreetmap-api-gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/openstreetmap-api-gateway.jar"]