# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache deps
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Build
COPY src ./src
RUN mvn -q -DskipTests package

# ---------- Run stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Le jar Spring Boot créé par spring-boot-maven-plugin
COPY --from=build /app/target/*.jar app.jar

# Optionnel (bonne pratique)
ENV JAVA_OPTS=""

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
