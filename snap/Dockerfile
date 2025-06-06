ARG JAVA_HOME=/opt/java
FROM --platform=linux/amd64 gradle:jdk16 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test


FROM ubuntu:latest

ARG JAVA_HOME

WORKDIR /opt/

## download open java
ADD https://cdn.azul.com/zulu/bin/zulu17.50.19-ca-jdk17.0.11-linux_x64.tar.gz /


RUN mkdir -p $JAVA_HOME/ && \
    tar -zxf /zulu17.50.19-ca-jdk17.0.11-linux_x64.tar.gz --strip-components 1  -C $JAVA_HOME  && \
    rm -f /zulu17.50.19-ca-jdk17.0.11-linux_x64.tar.gz

RUN mkdir -p /app/
COPY --from=build /home/gradle/src/build/libs/*.jar /app/
COPY ./elastic-apm-agent-1.52.1.jar /app/

WORKDIR /opt/

# setup paths
ENV PATH $PATH:$JAVA_HOME/bin


USER root

# Install Docker dependencies
RUN apt-get update && \
    apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release buildah && \
    rm -rf /var/lib/apt/lists/*

RUN echo 'unqualified-search-registries = ["docker.io"]' > /etc/containers/registries.conf