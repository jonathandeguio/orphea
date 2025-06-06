#ARG JDK_PACKAGE=openjdk-14.0.2_linux-x64_bin.tar.gz
ARG JDK_PACKAGE=openjdk-11.0.2_linux-x64_bin.tar.gz
ARG SPARK_HOME=/opt/spark
ARG JAVA_HOME=/opt/java
ARG SPARK_VERSION=3.3.4
ARG SPARK_PACKAGE=spark-$SPARK_VERSION-bin-hadoop3.tgz
#https://dlcdn.apache.org/spark/spark-3.3.4/spark-3.3.4-bin-hadoop3.tgz
# https://dlcdn.apache.org/spark/spark-3.4.1/spark-3.4.1-bin-hadoop3.tgz
# https://dlcdn.apache.org/spark/spark-3.3.0/spark-3.3.0-bin-hadoop3.tgz
#https://dlcdn.apache.org/spark/spark-3.3.1/spark-3.3.1-bin-hadoop3.tgz

# Base image stage 2
FROM --platform=linux/amd64 ubuntu as spark

ARG JAVA_HOME
ARG SPARK_HOME
ARG SPARK_PACKAGE
ARG SPARK_VERSION

WORKDIR /opt/

## download spark
ADD https://dlcdn.apache.org/spark/spark-$SPARK_VERSION/$SPARK_PACKAGE /

RUN mkdir -p $SPARK_HOME/  && \
    tar -zxf /$SPARK_PACKAGE --strip-components 1  -C $SPARK_HOME  && \
    rm -f $SPARK_PACKAGE

FROM --platform=linux/amd64 gradle:jdk11
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test

RUN mkdir /app/
RUN mv /home/gradle/src/build/libs/*.jar /app/

#ARG JAVA_HOME
ARG SPARK_HOME
ARG SPARK_PACKAGE
ARG SPARK_VERSION

WORKDIR /opt/

# get artifacts from previous stages
COPY --from=spark $SPARK_HOME $SPARK_HOME

ENV SPARK_HOME $SPARK_HOME

# setup paths
ENV PATH $PATH:$JAVA_HOME/bin
ENV PATH $PATH:$SPARK_HOME/bin
ENV PYTHONPATH $PYTHONPATH:$SPARK_HOME/python:$SPARK_HOME/python/lib/py4j-0.10.4-src.zip

# adding custom jars for backing fs and db etc
ADD https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/$SPARK_VERSION/hadoop-aws-$SPARK_VERSION.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/hadoop-aws-$SPARK_VERSION.jar
ADD https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.12.99/aws-java-sdk-bundle-1.12.99.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/aws-java-sdk-bundle-1.12.99.jar
ADD https://storage.googleapis.com/hadoop-lib/gcs/gcs-connector-hadoop3-latest.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/gcs-connector-hadoop3-latest.jar
ADD https://repo1.maven.org/maven2/com/google/cloud/google-cloud-storage/2.20.1/google-cloud-storage-2.20.1.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/google-cloud-storage-2.20.1.jar
ADD https://repo1.maven.org/maven2/io/minio/minio/8.5.2/minio-8.5.2.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/minio-8.5.2.jar

ADD https://repo1.maven.org/maven2/org/eclipse/jgit/org.eclipse.jgit/6.5.0.202303070854-r/org.eclipse.jgit-6.5.0.202303070854-r.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/org.eclipse.jgit-6.5.0.202303070854-r.jar

ADD https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/1.16.0/google-auth-library-credentials-1.16.0.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/google-auth-library-credentials-1.16.0.jar

ADD https://repo1.maven.org/maven2/com/google/auth/google-auth-library-oauth2-http/1.16.0/google-auth-library-oauth2-http-1.16.0.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/google-auth-library-oauth2-http-1.16.0.jar


ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.5.4/postgresql-42.5.4.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/postgresql-42.5.4.jar


USER root
RUN apt-get update --allow-releaseinfo-change \
    && apt-get update \
    && apt-get install -y openssl curl tini \
    && rm -rf /var/lib/apt/lists/*

COPY entrypoint.sh /usr/bin/
RUN chmod +x /usr/bin/entrypoint.sh
ENTRYPOINT ["/usr/bin/entrypoint.sh"]
USER ${spark_uid}



