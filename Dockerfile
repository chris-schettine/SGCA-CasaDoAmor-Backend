FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim

VOLUME /tmp

ARG JAR_FILE=target/sgca-backend-0.0.1-SNAPSHOT.jar

COPY --from=build /app/${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]