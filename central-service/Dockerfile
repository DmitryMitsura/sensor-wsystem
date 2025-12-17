FROM gradle:8.7-jdk21 AS build

WORKDIR /workspace
COPY . .
RUN gradle :central-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/central-service/build/libs/central-service.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
