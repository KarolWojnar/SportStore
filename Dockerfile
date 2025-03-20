FROM amazoncorretto:17

WORKDIR /app

COPY .env /app/.env
COPY build/libs/sport-web-store-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8080