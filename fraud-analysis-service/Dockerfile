FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY ../pom.xml ./pom.xml

COPY fraud-analysis-service/pom.xml ./fraud-analysis-service/pom.xml
COPY fraud-analysis-service/src ./fraud-analysis-service/src

WORKDIR /build/fraud-analysis-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/fraud-analysis-service/target/fraud-analysis-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]