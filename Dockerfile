FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x ./gradlew

COPY src src

RUN ./gradlew build -x test --no-daemon


FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

ENV TZ=Asia/Jakarta
ENV JAVA_OPTS=""

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
