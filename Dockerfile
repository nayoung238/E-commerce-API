FROM openjdk:21-ea-11-jdk-slim

VOLUME /tmp

COPY /build/libs/item-service-0.0.1-SNAPSHOT.jar ItemService.jar

ENTRYPOINT ["java", "-jar", "ItemService.jar"]