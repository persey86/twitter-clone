FROM gradle:7-jdk17 AS builder
WORKDIR /app
COPY . .
RUN /app/gradlew bootJar --no-daemon

FROM openjdk:17-jdk-slim
RUN addgroup --system userapp && adduser -system --no-create-home --uid 1001 userapp --ingroup userapp
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
