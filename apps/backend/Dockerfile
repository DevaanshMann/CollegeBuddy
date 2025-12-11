# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy the built jar
COPY --from=build /app/target/api-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseZGC"

EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8081}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8081} -jar app.jar"]