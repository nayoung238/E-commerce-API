FROM openjdk:21-ea-11-jdk-slim

VOLUME /tmp

COPY /build/libs/item-service-1.0.jar ItemService.jar

ENTRYPOINT ["java", "-jar", "ItemService.jar"]