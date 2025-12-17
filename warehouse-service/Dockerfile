FROM gradle:8.7-jdk21 AS build

WORKDIR /workspace
COPY . .
RUN gradle :warehouse-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/warehouse-service/build/libs/warehouse-service.jar app.jar

EXPOSE 8081
EXPOSE 3344/udp
EXPOSE 3355/udp

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
