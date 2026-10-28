# ---------- Stage 1: build the JAR ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -q clean package -Dmaven.test.skip=true

# ---------- Stage 2: run the JAR ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --no-create-home --shell /usr/sbin/nologin appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-XX:+UseSerialGC", "-jar", "app.jar"]
