FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY backend/pom.xml backend/pom.xml
RUN mvn -B -pl backend -am dependency:go-offline
COPY backend/src backend/src
RUN mvn -B -pl backend -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S takeaway && adduser -S takeaway -G takeaway
WORKDIR /app
COPY --from=build /workspace/backend/target/takeaway-backend-*.jar app.jar
RUN mkdir -p /data/uploads && chown -R takeaway:takeaway /app /data
USER takeaway
ENV APP_STORAGE_DIRECTORY=/data/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
