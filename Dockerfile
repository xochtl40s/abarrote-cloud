# ============================================================
# ETAPA 1: COMPILACIÓN
# ============================================================

FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Primero se copia el POM para aprovechar la caché de Docker.
COPY pom.xml .

RUN mvn \
    -B \
    -DskipTests \
    dependency:go-offline

# Después se copia el código fuente.
COPY src ./src

RUN mvn \
    -B \
    -DskipTests \
    clean package

# ============================================================
# ETAPA 2: EJECUCIÓN
# ============================================================

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Usuario sin privilegios para ejecutar la aplicación.
RUN groupadd \
        --system \
        abarrote \
    && useradd \
        --system \
        --gid abarrote \
        --home-dir /app \
        abarrote

COPY --from=build \
    /app/target/*.jar \
    /app/abarrote-cloud.jar

RUN chown \
    abarrote:abarrote \
    /app/abarrote-cloud.jar

USER abarrote

ENV SPRING_PROFILES_ACTIVE=render

ENV JAVA_TOOL_OPTIONS="\
-XX:MaxRAMPercentage=75.0 \
-XX:InitialRAMPercentage=25.0 \
-XX:+UseSerialGC \
-Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT [\
    "java",\
    "-jar",\
    "/app/abarrote-cloud.jar"\
]

