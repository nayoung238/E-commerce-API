FROM openjdk:21-ea-11-jdk-slim

VOLUME /tmp

COPY /build/libs/account-service-1.0.jar AccountService.jar

ENTRYPOINT ["java", "-jar", "AccountService.jar"]