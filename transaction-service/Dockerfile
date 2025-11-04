FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY ../pom.xml ./pom.xml

COPY transaction-service/pom.xml ./transaction-service/pom.xml
COPY transaction-service/src ./transaction-service/src

WORKDIR /build/transaction-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/transaction-service/target/transaction-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]