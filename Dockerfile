FROM maven:3.9.9-amazoncorretto-21-alpine as build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-slim-bookworm
WORKDIR /app
COPY --from=build /app/target/*.jar fasttrade.jar
ENTRYPOINT ["java", "-jar", "fasttrade.jar"]
