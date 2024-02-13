FROM openjdk:21-jdk-slim
VOLUME /tmp
WORKDIR /app

ARG JAR_FILE=/build/libs/*.jar
ARG JAR_DEST=/app/app.jar
COPY ${JAR_FILE} ${JAR_DEST}
COPY src/main/resources/application.yml application.yml
COPY src/main/resources/application-prod.yml application-prod.yml

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "-Dspring.config.name=application,application-prod", "app.jar"]