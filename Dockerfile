FROM maven:3.9.4-eclipse-temurin-17 as builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean install -DskipTests

FROM quay.io/keycloak/keycloak:26.3.3

COPY --from=builder /app/target/custom-token-mapper-1.0.jar /opt/keycloak/providers/

RUN /opt/keycloak/bin/kc.sh build
