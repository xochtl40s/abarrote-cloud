# ============================================================
# ABARROTE CLOUD - RENDER FREE
# Java 17 / Spring Boot / 512 MB RAM
# ============================================================

# ------------------------------------------------------------
# ETAPA 1: COMPILACIÓN
# ------------------------------------------------------------

FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src

RUN mvn -B -DskipTests clean package

# ------------------------------------------------------------
# ETAPA 2: EJECUCIÓN
# ------------------------------------------------------------

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system abarrote \
    && useradd \
        --system \
        --gid abarrote \
        --home-dir /app \
        --shell /usr/sbin/nologin \
        abarrote

COPY --from=build /app/target/*.jar /app/abarrote-cloud.jar

RUN chown abarrote:abarrote /app/abarrote-cloud.jar

USER abarrote

ENV SPRING_PROFILES_ACTIVE=render

ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx256m -XX:MaxMetaspaceSize=96m -XX:ReservedCodeCacheSize=48m -Xss256k -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError -Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/abarrote-cloud.jar"]
