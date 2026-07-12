# ---------- test stage: full suite + 80% JaCoCo gate (CI target, not part of the shipped image) ----------
FROM maven:3.9-eclipse-temurin-25 AS test
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn verify -B

# ---------- build stage: compile + package only, no test code involved ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src/main ./src/main
RUN mvn clean package -DskipTests -Djacoco.skip=true -B

# ---------- layers stage: split the fat jar into cache-friendly layers (deps rarely change, app code does) ----------
FROM build AS layers
WORKDIR /app
RUN java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted

# ---------- jre-build stage: jlink a custom JRE containing only the JDK modules this app actually uses ----------
FROM eclipse-temurin:25-jdk AS jre-build
WORKDIR /app
COPY --from=layers /app/extracted/dependencies/ ./
COPY --from=layers /app/extracted/application/ ./
RUN jdeps --ignore-missing-deps -q \
      --multi-release 25 \
      --recursive \
      --print-module-deps \
      --class-path 'BOOT-INF/lib/*' \
      BOOT-INF/classes > /modules.txt \
    && jlink \
      --add-modules $(cat /modules.txt) \
      --strip-debug \
      --no-man-pages \
      --no-header-files \
      --compress=zip-6 \
      --output /customjre

# ---------- runtime stage: minimal OS + custom JRE + non-root user — this is the image k8s actually deploys ----------
FROM debian:bookworm-slim AS runtime
ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /customjre $JAVA_HOME

RUN groupadd --gid 1000 spring \
    && useradd --uid 1000 --gid spring --shell /bin/false --create-home spring

WORKDIR /app
COPY --from=layers --chown=spring:spring /app/extracted/dependencies/ ./
COPY --from=layers --chown=spring:spring /app/extracted/spring-boot-loader/ ./
COPY --from=layers --chown=spring:spring /app/extracted/snapshot-dependencies/ ./
COPY --from=layers --chown=spring:spring /app/extracted/application/ ./

USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]