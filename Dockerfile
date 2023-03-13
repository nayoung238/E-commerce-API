FROM openjdk:21-ea-11-jdk-slim

VOLUME /tmp

COPY /build/libs/api-gateway-service-1.0.jar ApiGatewayService.jar

ENTRYPOINT ["java", "-jar", "ApiGatewayService.jar"]