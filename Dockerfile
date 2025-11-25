FROM gradle:8-jdk17 AS builder

WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/assessment-0.0.1-SNAPSHOT.jar /app
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/assessment-0.0.1-SNAPSHOT.jar"]
