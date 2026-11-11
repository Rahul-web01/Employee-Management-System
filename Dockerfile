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

# Render's free plan gives only 0.1 CPU, where a plain Spring Boot start takes ~6 minutes
# and Render's port scan gives up first. Two things cut that to ~2 minutes:
#  1. Class Data Sharing (CDS): a one-off training run at build time records the loaded
#     classes into app.jsa, so the real start skips most class loading and verification.
#  2. -XX:TieredStopAtLevel=1: use only the fast C1 JIT, which spends far less CPU at startup.
# The training run stops right after the Spring context starts and never touches a database.
RUN java -Djarmode=tools -jar app.jar extract --destination extracted \
 && rm app.jar \
 && java -XX:ArchiveClassesAtExit=extracted/app.jsa -Xlog:cds=error -XX:+UseSerialGC -XX:TieredStopAtLevel=1 \
      -Dspring.context.exit=onRefresh \
      -Dspring.jpa.hibernate.ddl-auto=none \
      -Dspring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false \
      -Dspring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect \
      -jar extracted/app.jar

USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:SharedArchiveFile=extracted/app.jsa", "-XX:MaxRAMPercentage=70", "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1", "-jar", "extracted/app.jar"]
