FROM openjdk:21-ea-11-jdk-slim

VOLUME /tmp

COPY /build/libs/account-service-0.0.1-SNAPSHOT.jar AccountService.jar

ENTRYPOINT ["java", "-jar", "AccountService.jar"]