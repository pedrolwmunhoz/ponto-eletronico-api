# Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY pom.xml .
# Download dependencies (cache layer)
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ENV PORT=8081
EXPOSE 8081

COPY --from=build /app/target/*.jar app.jar
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
