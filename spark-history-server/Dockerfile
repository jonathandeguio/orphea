ARG SPARK_HOME=/opt/spark
ARG JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
ARG SPARK_VERSION=3.1.2
ARG SPARK_RELEASE=https://codeload.github.com/apache/spark/tar.gz/refs/tags/v$SPARK_VERSION

############################################
###  Install spark
############################################

# Base image
FROM --platform=linux/amd64 ubuntu as spark
ENV DEBIAN_FRONTEND noninteractive TZ=Etc/UTC

ARG JAVA_HOME
ARG SPARK_HOME
ARG SPARK_VERSION
ARG SPARK_RELEASE

RUN apt-get update --allow-releaseinfo-change \
    && apt-get update \
    && apt-get install -yq openssl git curl wget openjdk-11-jdk \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/

## download spark
RUN mkdir /spark-source
WORKDIR /spark-source
ADD ${SPARK_RELEASE} /spark-${SPARK_VERSION}.tar.gz


# RUN ls -l /

COPY ./* /

RUN tar zxf /spark-${SPARK_VERSION}.tar.gz

WORKDIR /spark-source/spark-${SPARK_VERSION}

COPY ./spark-modified/core/src/main/resources/org/apache/spark/ui/static/ /spark-source/spark-3.1.2/core/src/main/resources/org/apache/spark/ui/static/
COPY ./spark-modified/core/src/main/scala/org/apache/spark/ui/ /spark-source/spark-3.1.2/core/src/main/scala/org/apache/spark/ui/
COPY ./spark-modified/core/src/main/scala/org/apache/spark/ui/jobs/ /spark-source/spark-3.1.2/core/src/main/scala/org/apache/spark/ui/jobs/
COPY ./spark-modified/core/src/main/scala/org/apache/spark/deploy/history /spark-source/spark-3.1.2/core/src/main/scala/org/apache/spark/deploy/history
COPY ./spark-modified/core/src/main/scala/org/apache/spark/status/api/v1 /spark-source/spark-3.1.2/core/src/main/scala/org/apache/spark/status/api/v1
COPY ./spark-modified/pom.xml /spark-source/spark-3.1.2/

RUN  ./dev/make-distribution.sh --tgz -Dhadoop.version=${SPARK_VERSION} -DskipTests && \
    mkdir -p $SPARK_HOME/  && \
    tar -zxf /spark-source/spark-3.1.2/spark-3.1.2-bin-3.1.2.tgz --strip-components 1  -C $SPARK_HOME && \
    rm -rf /spark-source && \
    rm -rf /spark-modified && \
    rm /spark-${SPARK_VERSION}.tar.gz


############################################
###  sparkimage
############################################

FROM --platform=linux/amd64 ubuntu as sparkimage
ENV DEBIAN_FRONTEND noninteractive TZ=Etc/UTC

ARG JAVA_HOME 
ARG SPARK_HOME 
ARG SPARK_VERSION

WORKDIR /opt/

# get artifacts from previous stages 
COPY --from=spark $SPARK_HOME $SPARK_HOME

# Setup JAVA_HOME, this is useful for docker commandline 
ENV JAVA_HOME $JAVA_HOME 
ENV SPARK_HOME $SPARK_HOME

# setup paths 
ENV PATH $PATH:$JAVA_HOME/bin 
ENV PATH $PATH:$SPARK_HOME/bin 
ENV PYTHONPATH $PYTHONPATH:$SPARK_HOME/python:$SPARK_HOME/python/lib/py4j-0.10.4-src.zip

# adding S3 jars
ADD https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/$SPARK_VERSION/hadoop-aws-$SPARK_VERSION.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/hadoop-aws-$SPARK_VERSION.jar
ADD https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.12.99/aws-java-sdk-bundle-1.12.99.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/aws-java-sdk-bundle-1.12.99.jar

USER ${spark_uid}

FROM --platform=linux/amd64 sparkimage
USER root
RUN apt-get update --allow-releaseinfo-change \
    && apt-get update \
    && apt-get install -y openssl curl openjdk-11-jdk tini \
    && rm -rf /var/lib/apt/lists/*

COPY ./spark-modified/spark-defaults.conf /opt/spark/conf/

COPY ./spark-modified/google_creds.json /opt/spark/conf/

ENV SPARK_NO_DAEMONIZE TRUE

ENTRYPOINT ["/opt/spark/sbin/start-history-server.sh"]