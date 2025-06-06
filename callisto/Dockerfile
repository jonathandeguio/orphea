FROM python:3.10

RUN git clone --depth=1 https://github.com/Bash-it/bash-it.git ~/.bash_it && \
  bash ~/.bash_it/install.sh --silent

# COPY dependency files
RUN mkdir dependencies
COPY dependencies/packages.txt dependencies/
COPY dependencies/requirements.txt dependencies/

RUN curl -sL https://deb.nodesource.com/setup_12.x | bash - && \
  apt-get -qq upgrade -y && \
  apt-get install -qq -y $(cat dependencies/packages.txt) && \
  rm -rf /var/lib/apt/lists/*

RUN pip install -q --upgrade pip && \
  pip install -q --upgrade -r dependencies/requirements.txt

#RUN  jupyter labextension install \
#     @jupyterlab/completer \
#     @jupyterlab/completer-extension



#RUN  jupyter labextension install \
#    @jupyter-widgets/jupyterlab-manager \
#    @jupyterlab/latex \
#    jupyterlab-drawio \
#    jupyterlab-plotly
#    @bokeh/jupyter_bokeh \
#    @krassowski/jupyterlab-lsp
#    @jupyterlab/git \
#    jupyterlab-spreadsheet


# Spark dependencies
# Default values can be overridden at build time
# (ARGS are in lower case to distinguish them from ENV)
#https://archive.apache.org/dist/spark/spark-3.3.4/spark-3.3.4-bin-hadoop3.tgz
ARG spark_version="3.3.4"
ARG SPARK_VERSION="3.3.4"
ARG hadoop_version="3"
ARG scala_version
#ARG spark_checksum="1e8234d0c1d2ab4462d6b0dfe5b54f2851dcd883378e0ed756140e10adfb5be4123961b521140f580e364c239872ea5a9f813a20b73c69cb6d4e95da2575c29c"
ARG openjdk_version="17"

ENV APACHE_SPARK_VERSION="${spark_version}" \
    HADOOP_VERSION="${hadoop_version}"

RUN apt-get update --yes && \
    apt-get install --yes --no-install-recommends \
    "openjdk-${openjdk_version}-jre-headless" \
    ca-certificates-java && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Spark installation
WORKDIR /tmp

RUN if [ -z "${scala_version}" ]; then \
#    https://dlcdn.apache.org/spark/spark-3.3.4/spark-3.3.4-bin-hadoop3.tgz \
#    https://archive.apache.org/dist/spark/spark-3.3.4/spark-3.3.4-bin-hadoop3.tgz
    wget -qO "spark.tgz" "https://archive.apache.org/dist/spark/spark-${APACHE_SPARK_VERSION}/spark-${APACHE_SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz"; \
  else \
    wget -qO "spark.tgz" "archive.apache.org/dist/spark/spark-${APACHE_SPARK_VERSION}/spark-${APACHE_SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}-scala${scala_version}.tgz"; \
  fi && \
#  echo "${spark_checksum} *spark.tgz" | sha512sum -c - && \
  tar xzf "spark.tgz" -C /usr/local --owner root --group root --no-same-owner && \
  rm "spark.tgz"


# Configure Spark
ENV SPARK_HOME=/usr/local/spark
ENV SPARK_OPTS="--driver-java-options=-Xms1024M --driver-java-options=-Xmx4096M --driver-java-options=-Dlog4j.logLevel=info" \
    PATH="${PATH}:${SPARK_HOME}/bin:/usr/local/bin"

RUN if [ -z "${scala_version}" ]; then \
    ln -s "spark-${APACHE_SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}" "${SPARK_HOME}"; \
  else \
    ln -s "spark-${APACHE_SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}-scala${scala_version}" "${SPARK_HOME}"; \
  fi && \
  # Add a link in the before_notebook hook in order to source automatically PYTHONPATH && \
  mkdir -p /usr/local/bin/before-notebook.d && \
  ln -s "${SPARK_HOME}/sbin/spark-config.sh" /usr/local/bin/before-notebook.d/spark-config.sh


# adding S3 jars
ADD https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/$SPARK_VERSION/hadoop-aws-$SPARK_VERSION.jar $SPARK_HOME/jars
ADD https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.12.99/aws-java-sdk-bundle-1.12.99.jar $SPARK_HOME/jars
ADD https://storage.googleapis.com/hadoop-lib/gcs/gcs-connector-hadoop3-latest.jar $SPARK_HOME/jars

RUN chmod 644 $SPARK_HOME/jars/hadoop-aws-$SPARK_VERSION.jar \
 && chmod 644 $SPARK_HOME/jars/aws-java-sdk-bundle-1.12.99.jar \
 && chmod 644 $SPARK_HOME/jars/gcs-connector-hadoop3-latest.jar


WORKDIR /opt/
RUN git clone https://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@github.com/Bosler-io/boson

RUN pip install /opt/boson/funnel/

COPY sh/entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/entrypoint.sh
COPY config/ /root/.jupyter/

# Configure IPython system-wide
COPY config/jupyter_server_config.py "/etc/jupyter/"
COPY config/customauthenticator.py "/etc/jupyter/"
COPY config/ipython_kernel_config.py "/etc/ipython/"

RUN mkdir -p /root/.ipython/profile_default
COPY config/ipython_config.py /root/.ipython/profile_default/ipython_config.py

RUN mkdir -p ~/.jupyter/custom/
RUN mkdir -p ~/.jupyter/lab/settings/
RUN mkdir -p /usr/local/share/jupyter/lab/settings/
COPY config/overrides.json /usr/local/share/jupyter/lab/settings/
COPY config/overrides.json ~/.jupyter/lab/settings/
COPY config/overrides.json "/etc/jupyter/"
COPY patches/custom.css ~/.jupyter/custom/

# Warning suppress
RUN mkdir -p ~/.ipython/profile_default/startup
COPY config/01-suppress-warnings.py /root/.ipython/profile_default/startup/


# Copy patch
COPY patches/hash_problem.py /usr/local/lib/python3.10/site-packages/jupyter_server/services/contents/handlers.py
# Jupyter css patch
COPY patches/index.css /usr/local/lib/python3.10/site-packages/jupyter_server/static/style/index.css
COPY patches/index.css /usr/local/lib/python3.10/site-packages/jupyterlab/themes/@jupyterlab/theme-dark-extension/index.css
COPY patches/index.css /usr/local/lib/python3.10/site-packages/jupyterlab/themes/@jupyterlab/theme-light-extension/index.css
COPY patches/index.css /usr/local/share/jupyter/lab/themes/@jupyterlab/theme-dark-extension/index.css
COPY patches/index.css /usr/local/share/jupyter/lab/themes/@jupyterlab/theme-light-extension/index.css
COPY patches/index.css /usr/local/share/jupyter/nbconvert/templates/lab/static/index.css


EXPOSE 8686
VOLUME /notebooks
WORKDIR /notebooks
ENTRYPOINT ["entrypoint.sh"]