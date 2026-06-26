FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /build/target/java-api-1.0.0.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
