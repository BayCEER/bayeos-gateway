# syntax=docker/dockerfile:1
# BayEOS Gateway - container image
#
# Build context is bayeos-gateway/. Neither dependency below is on Maven Central;
# both are cloned and installed from their public GitHub repositories:
#   - de.unibayreuth.bayceer.bayeos:bayeos-frame-parser:1.4.2      ref: mai
#     https://github.com/BayCEER/bayeos-frame-parser
#   - de.unibayreuth.bayceer.bayeos:bayeos-api-java:1.0.0-SNAPSHOT ref: main
#     https://github.com/BayCEER/bayeos-api-java
#
#   docker compose build bayeos-gateway
#   docker build -t bayeos-gateway bayeos-gateway

FROM maven:3.9-eclipse-temurin-11 AS builder
WORKDIR /build

RUN apt-get update \
	&& apt-get install -y --no-install-recommends git ca-certificates \
	&& rm -rf /var/lib/apt/lists/*

# 1. bayeos-frame-parser
ARG FRAME_PARSER_REPO=https://github.com/BayCEER/bayeos-frame-parser.git
ARG FRAME_PARSER_REF=main
RUN git clone --depth 1 --branch "${FRAME_PARSER_REF}" "${FRAME_PARSER_REPO}" /tmp/frame-parser \
	&& mvn -q -f /tmp/frame-parser/pom.xml -DskipTests install

# 2. bayeos-api-java
ARG API_JAVA_REPO=https://github.com/BayCEER/bayeos-api-java.git
ARG API_JAVA_REF=main
RUN git clone --depth 1 --branch "${API_JAVA_REF}" "${API_JAVA_REPO}" /tmp/api-java \
	&& mvn -q -f /tmp/api-java/pom.xml -DskipTests install

# 3. bayeos-gateway (Flyway migrations are bundled under classpath:db/migration)
COPY pom.xml .
COPY src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:11-jre

RUN set -eux; \
	groupadd --gid 10001 bayeos; \
	useradd --uid 10001 --gid bayeos --home-dir /var/lib/bayeos-gateway --create-home bayeos; \
	mkdir -p /data/uploads; \
	chown -R bayeos:bayeos /var/lib/bayeos-gateway /data

COPY --from=builder /build/target/bayeos-gateway.jar /var/lib/bayeos-gateway/bayeos-gateway.jar

USER bayeos
WORKDIR /var/lib/bayeos-gateway

EXPOSE 5533

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "/var/lib/bayeos-gateway/bayeos-gateway.jar"]
