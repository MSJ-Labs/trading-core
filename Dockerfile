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