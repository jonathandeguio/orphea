ARG DEBIAN_FRONTEND=noninteractive
ARG JDK_PACKAGE=openjdk-14.0.2_linux-x64_bin.tar.gz
ARG SPARK_HOME=/opt/spark
ARG JAVA_HOME=/opt/java
ARG SPARK_VERSION=3.3.4
ARG SPARK_PACKAGE=spark-$SPARK_VERSION-bin-hadoop3.tgz
#https://dlcdn.apache.org/spark/spark-3.4.1/spark-3.4.1-bin-hadoop3.tgz
# https://dlcdn.apache.org/spark/spark-3.3.3/spark-3.3.3-bin-hadoop3.tgz
#https://archive.apache.org/dist/spark/spark-3.3.4/spark-3.3.4-bin-hadoop3.tgz

############################################
###  Install openjava
############################################

# Base image stage 1
FROM --platform=linux/amd64 ubuntu as jdk

ARG JAVA_HOME
ARG JDK_PACKAGE

WORKDIR /opt/

## download open java
ADD https://download.java.net/java/GA/jdk14.0.2/205943a0976c4ed48cb16f1043c5c647/12/GPL/$JDK_PACKAGE /


RUN mkdir -p $JAVA_HOME/ && \
    tar -zxf /$JDK_PACKAGE --strip-components 1  -C $JAVA_HOME  && \
    rm -f $JDK_PACKAGE


############################################
###  Install spark
############################################

# Base image stage 2
FROM --platform=linux/amd64 ubuntu as spark

ARG JAVA_HOME
ARG SPARK_HOME
ARG SPARK_PACKAGE
ARG SPARK_VERSION

WORKDIR /opt/

## download spark
#ADD https://dlcdn.apache.org/spark/spark-$SPARK_VERSION/$SPARK_PACKAGE /
ADD https://archive.apache.org/dist/spark/spark-$SPARK_VERSION/$SPARK_PACKAGE /

RUN mkdir -p $SPARK_HOME/  && \
    tar -zxf /$SPARK_PACKAGE --strip-components 1  -C $SPARK_HOME  && \
    rm -f $SPARK_PACKAGE


############################################
###  final
############################################

FROM --platform=linux/amd64 ubuntu as sparkimage

ARG JAVA_HOME
ARG SPARK_HOME
ARG SPARK_PACKAGE
ARG SPARK_VERSION

WORKDIR /opt/

# get artifacts from previous stages
COPY --from=jdk $JAVA_HOME $JAVA_HOME
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
ADD https://storage.googleapis.com/hadoop-lib/gcs/gcs-connector-hadoop3-latest.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/gcs-connector-hadoop3-latest.jar


# Cleanup
RUN  apt-get clean && \
    rm -rf /var/lib/ap

# COPY dependency files
RUN mkdir dependencies
COPY orphea/dependencies/packages.txt dependencies/
COPY orphea/dependencies/requirements.txt dependencies/

RUN \
    apt-get -qq upgrade -y && \
    apt-get -qq update -y && \
    DEBIAN_FRONTEND=noninteractive apt-get install -qq -y $(cat dependencies/packages.txt) && \
    rm -rf /var/lib/apt/lists/*

RUN pip install -q --upgrade pip && \
  pip install -q --upgrade -r dependencies/requirements.txt


WORKDIR /opt/
RUN mkdir /opt/funnel/
COPY ./orphea /opt/funnel

## Install PySpark and Numpy
RUN \
    pip3 install --upgrade pip && \
    pip3 -q install /opt/funnel/


USER ${spark_uid}

FROM sparkimage
USER root

COPY entrypoint.sh /usr/bin/
ENTRYPOINT ["/usr/bin/entrypoint.sh"]
