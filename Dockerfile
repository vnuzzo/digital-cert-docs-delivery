ARG MAVEN_IMAGE=maven:3.9.9-eclipse-temurin-21
ARG RUNTIME_IMAGE=eclipse-temurin:21-jre

FROM ${MAVEN_IMAGE} AS build
WORKDIR /workspace

ARG MODULE
RUN test -n "${MODULE}"

COPY . .

RUN mvn -q -DskipTests -pl ${MODULE} -am clean package

FROM ${RUNTIME_IMAGE} AS runtime
WORKDIR /app

ARG MODULE
RUN test -n "${MODULE}"

ARG APP_PORT=8080

RUN useradd -r -u 10001 appuser && chown -R appuser:appuser /app
USER appuser

COPY --from=build /workspace/${MODULE}/target/*.jar /app/app.jar

EXPOSE ${APP_PORT}
ENTRYPOINT ["java","-jar","/app/app.jar"]