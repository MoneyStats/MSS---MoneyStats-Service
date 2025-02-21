FROM maven:3.9.7-eclipse-temurin-22 AS build
COPY . .
RUN mvn clean package

FROM eclipse-temurin:22-jdk
EXPOSE 8080
WORKDIR /
COPY --from=build /target/moneystats-service.jar moneystats-service.jar

ENTRYPOINT ["java","-jar","moneystats-service.jar"]
ENV TZ Europe/Rome
