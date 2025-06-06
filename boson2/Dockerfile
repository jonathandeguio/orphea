FROM --platform=linux/amd64 gradle:jdk11 AS build-stage
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test
#RUN gradle shadowJar

FROM --platform=linux/amd64 openjdk:11 as production-stage

RUN mkdir /app/
COPY --from=build-stage /home/gradle/src/build/libs/*.jar /app/
COPY src/main/resources/application.yml /app/
COPY ./wait-for-it.sh /app/