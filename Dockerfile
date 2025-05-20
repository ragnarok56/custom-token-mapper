# FROM quay.io/keycloak/keycloak:22.0.1

# COPY target/request-param-to-token-mapper-1.0.jar /opt/keycloak/providers/
# RUN /opt/keycloak/bin/kc.sh build

# 1. aşama: Maven ile projeyi build et
FROM maven:3.9.4-eclipse-temurin-17 as builder

WORKDIR /app

# POM ve kaynakları kopyala
COPY pom.xml .
COPY src ./src

# Projeyi package et (skip test için -DskipTests ekleyebilirsiniz)
RUN mvn clean install -DskipTests

# 2. aşama: Keycloak imajını kullan, jar'ı kopyala ve build et
FROM quay.io/keycloak/keycloak:22.0.1

COPY --from=builder /app/target/custom-token-mapper-1.0.jar /opt/keycloak/providers/

RUN /opt/keycloak/bin/kc.sh build
