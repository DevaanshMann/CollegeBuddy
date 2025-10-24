# Build & test with Java 21 + Maven
FROM maven:3.9-eclipse-temurin-21 AS test
WORKDIR /build
# copy multi-module poms first (better cache)
COPY pom.xml ./
COPY apps/api/pom.xml apps/api/pom.xml
# copy sources
COPY apps/api/src apps/api/src
# run unit tests for api module
RUN mvn -q -pl apps/api -am -DskipITs test