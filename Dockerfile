FROM openjdk:21-jdk
WORKDIR /app


COPY target/*.jar app.jar
COPY ../../habitback/Habitleague/.env .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]