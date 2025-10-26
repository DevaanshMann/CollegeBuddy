# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy pom + sources
COPY pom.xml ./
COPY src ./src

# Build a layered, slim jar (skip tests for faster image build)
RUN mvn -U -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseZGC" \
    SPRING_PROFILES_ACTIVE=prod

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]