FROM maven:3.8.2-jdk-11 AS build
COPY . .
RUN mvn clean package

FROM openjdk:11
EXPOSE 8080
WORKDIR /
COPY --from=build /target/moneystats-service.jar moneystats-service.jar

ENTRYPOINT ["java","-jar","moneystats-service.jar"]
ENV TZ Europe/Rome
