# ---------- Stage 1: build the JAR ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Download dependencies first (faster rebuilds)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: run the JAR ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
