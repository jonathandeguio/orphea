# Stage 1: Build
FROM gradle:jdk15 AS build-stage
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test
#RUN gradle shadowJar

# Stage 2: Production
FROM openjdk:15 as production-stage

# Install Helm at /app/helm
RUN mkdir -p /app
RUN curl -sSL -o /app/helm-v3.8.0-rc.1-linux-amd64.tar.gz https://get.helm.sh/helm-v3.8.0-rc.1-linux-amd64.tar.gz && \
    tar -zxvf /app/helm-v3.8.0-rc.1-linux-amd64.tar.gz -C /app && \
    mv /app/linux-amd64/helm /app/helm && \
    rm -rf /app/helm-v3.8.0-rc.1-linux-amd64.tar.gz /app/linux-amd64

COPY --from=build-stage /home/gradle/src/build/libs/*.jar /app/
COPY src/main/resources/application.yml /app/

# Set working directory
WORKDIR /app

# Optionally, add commands to run your application
CMD ["java", "-jar", "/app/capture-0.0.1-SNAPSHOT.jar"]
