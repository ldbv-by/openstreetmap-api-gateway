FROM azul/zulu-openjdk:24.0.2-24.32

RUN apt-get update && apt-get install -y \
    maven \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /src/openstreetmap-api-gateway
COPY pom.xml .
RUN mvn -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package && \
    install -d /app && \
    JAR="$(find target -maxdepth 1 -type f -name '*.jar' \
           ! -name '*-sources.jar' ! -name '*-javadoc.jar' | head -n 1)" && \
    cp "$JAR" /app/openstreetmap-api-gateway.jar

RUN rm -rf /src
WORKDIR /app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/openstreetmap-api-gateway.jar"]