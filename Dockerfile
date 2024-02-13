FROM openjdk:21-jdk-slim

WORKDIR /usr/src/app

VOLUME /tmp

COPY /build/libs/Order-service-0.0.1-SNAPSHOT.jar OrderService.jar

ENTRYPOINT ["java", "-jar", "OrderService.jar"]