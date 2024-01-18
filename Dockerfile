FROM openjdk:11-jdk-slim

VOLUME /tmp

COPY /build/libs/api-gateway-service-0.0.1-SNAPSHOT.jar ApiGatewayService.jar

ENTRYPOINT ["java", "-jar", "ApiGatewayService.jar"]