FROM gradle:9.2-jdk21-alpine AS builder
WORKDIR /app
COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon || true
ARG SCOPE
ARG SERVICE
COPY ${SCOPE}/${SERVICE}/src ./src
RUN ./gradlew :${SCOPE}:${SERVICE}:shadowJar --no-daemon
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
