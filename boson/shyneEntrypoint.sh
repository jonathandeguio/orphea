#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# echo commands to the terminal output
#set -ex

# Check whether there is a passwd entry for the container UID
myuid=$(id -u)
mygid=$(id -g)
# turn off -e for getent because it will return error code in anonymous uid case
set +e
uidentry=$(getent passwd $myuid)
set -e

# If there is no passwd entry for the container UID, attempt to create one
if [ -z "$uidentry" ] ; then
    if [ -w /etc/passwd ] ; then
        echo "$myuid:x:$myuid:$mygid:${SPARK_USER_NAME:-anonymous uid}:$SPARK_HOME:/bin/false" >> /etc/passwd
    else
        echo "Container ENTRYPOINT failed to add passwd entry for anonymous UID"
    fi
fi

if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=$(java -XshowSettings:properties -version 2>&1 > /dev/null | grep 'java.home' | awk '{print $3}')
fi

SPARK_CLASSPATH="$SPARK_CLASSPATH:${SPARK_HOME}/jars/*"
env | grep SPARK_JAVA_OPT_ | sort -t_ -k4 -n | sed 's/[^=]*=\(.*\)/\1/g' > java_opts.txt
if [ "$(command -v readarray)" ]; then
  readarray -t SPARK_EXECUTOR_JAVA_OPTS < java_opts.txt
else
  SPARK_EXECUTOR_JAVA_OPTS=("${(@f)$(< java_opts.txt)}")
fi

if [ -n "$SPARK_EXTRA_CLASSPATH" ]; then
  SPARK_CLASSPATH="$SPARK_CLASSPATH:$SPARK_EXTRA_CLASSPATH"
fi

if ! [ -z ${PYSPARK_PYTHON+x} ]; then
    export PYSPARK_PYTHON
fi
if ! [ -z ${PYSPARK_DRIVER_PYTHON+x} ]; then
    export PYSPARK_DRIVER_PYTHON
fi

# If HADOOP_HOME is set and SPARK_DIST_CLASSPATH is not set, set it here so Hadoop jars are available to the executor.
# It does not set SPARK_DIST_CLASSPATH if already set, to avoid overriding customizations of this value from elsewhere e.g. Docker/K8s.
if [ -n "${HADOOP_HOME}"  ] && [ -z "${SPARK_DIST_CLASSPATH}"  ]; then
  export SPARK_DIST_CLASSPATH="$($HADOOP_HOME/bin/hadoop classpath)"
fi

if ! [ -z ${HADOOP_CONF_DIR+x} ]; then
  SPARK_CLASSPATH="$HADOOP_CONF_DIR:$SPARK_CLASSPATH";
fi

if ! [ -z ${SPARK_CONF_DIR+x} ]; then
  SPARK_CLASSPATH="$SPARK_CONF_DIR:$SPARK_CLASSPATH";
elif ! [ -z ${SPARK_HOME+x} ]; then
  SPARK_CLASSPATH="$SPARK_HOME/conf:$SPARK_CLASSPATH";
fi

# Orphea Specific
# Define a function to convert a string to uppercase
uppercase() {
    echo "$1" | tr '[:lower:]' '[:upper:]'
}

function preview_error_log() {
    local status="$1"
    local stage="$2"
    local message="$3"
    local debug="$4"

    timestamp=$(date +"%y/%m/%d %H:%M:%S")
    uppercase_status=$(uppercase "$status")
    uppercase_stage=$(uppercase "$stage")

    payload="{\"status\": \"$uppercase_status\",\"stage\": \"$uppercase_stage\",\"message\": \"$message\",\"debug\": \"$debug\", \"repositoryId\": \"$REPOSITORY_ID\", \"scriptPath\": \"$SCRIPT_PATH\"}"

    api_response=$(curl -s -o /dev/null -w '%{http_code}' \
      -X POST \
      -H 'accept: */*' \
      -H "Authorization: Bearer $BUILD_TOKEN" \
      -H "Content-Type: application/json" \
      -H "Accept-Language: en-US,en;q=0.5" \
      -H "Source: $SOURCE" \
      -d "$payload" "$ORPHEA_API/api/funnel/$BUILD_ID/log")

    if [ "$api_response" -ne 200 ]; then
        error_timestamp=$(date +%s)
        error_message="{\"timestamp\":\"$error_timestamp\",\"status\":\"error\",\"type\":\"ORPHEA\",\"message\":\"Unable to call orphea backend API. $(curl -s -X POST -H \"Content-Type: application/json\" -d \"$payload\" \"$ORPHEA_API/api/build/$BUILD_ID/funnelLog\")\"}"
        echo "$error_message"
        echo "Exiting"
        exit 1
    fi

    if [ "$status" == "ERROR" ]; then
        echo "$log" >&2
        echo "Exiting"
        exit 1
    fi
}

function log() {
    local status="$1"
    local stage="$2"
    local message="$3"
    local debug="$4"

    timestamp=$(date +"%y/%m/%d %H:%M:%S")
    uppercase_status=$(uppercase "$status")
    uppercase_stage=$(uppercase "$stage")

    log="$timestamp $uppercase_status $uppercase_stage: $message $debug"
    echo "$log"

#    if [ "$BUILD_TYPE" = "PREVIEW" ]; then
#        # shellcheck disable=SC2086
#        if [ "$1" = "ERROR" ]; then
#          preview_error_log "$1" "$2" "$3" "$4"
#        fi
#
#        return
#    fi

    payload="{\"status\": \"$uppercase_status\",\"stage\": \"$uppercase_stage\",\"message\": \"$message\",\"debug\": \"$debug\"}"

    api_response=$(curl -s -o /dev/null -w '%{http_code}' \
      -X POST \
      -H 'accept: */*' \
      -H "Authorization: Bearer $BUILD_TOKEN" \
      -H "Content-Type: application/json" \
      -H "Accept-Language: en-US,en;q=0.5" \
      -H "Source: $SOURCE" \
      -d "$payload" "$ORPHEA_API/api/build/$BUILD_ID/log")

    if [ "$api_response" -ne 200 ]; then
        error_timestamp=$(date +%s)
        error_message="{\"timestamp\":\"$error_timestamp\",\"status\":\"error\",\"type\":\"ORPHEA\",\"message\":\"Unable to call orphea backend API. $(curl -s -X POST -H \"Content-Type: application/json\" -d \"$payload\" \"$ORPHEA_API/api/build/$BUILD_ID/funnelLog\")\"}"
        echo "$error_message"
        echo "Exiting"
        exit 1
    fi

    if [ "$status" == "ERROR" ]; then
        echo "$log" >&2
        echo "Exiting"
        exit 1
    fi
}

function install_dependencies() {

  REPOSITORY_DIR=$1

  if [[ -v PASSWORD ]]; then
    PASSWORD=$(python -c "import IPython; print(IPython.lib.security.passwd('$PASSWORD'))")
    CMD="$CMD --NotebookApp.token='' --NotebookApp.password='${PASSWORD}'"
  fi

  if [[ -v GIT_URL ]]; then
    git clone "${GIT_URL}" "${REPOSITORY_DIR}" >> /dev/null
  fi

  if [ -f "$REPOSITORY_DIR"/packages.txt ]; then
    log "info" "PREPARING" "Found packages.txt file. Executing it to install apt packages."
    if ! apt-get -qq update; then
      log "ERROR" "FINISHED" "Processing Failed!"
      echo "Processing Failed!"
    fi
    if ! cat "${REPOSITORY_DIR}"/packages.txt |grep -v "^#" | xargs apt-get -qq install -y; then
      log "ERROR" "FINISHED" "Processing Failed!"
      echo "Processing Failed!"
    fi
#  else
#    log "info" "PREPARING" "packages.txt not found --> Continuing"
  fi

  if [ -f "$REPOSITORY_DIR"/requirements.txt ]; then
    if grep -q '^[^#]' "$REPOSITORY_DIR"/requirements.txt; then
      log "info" "PREPARING" "Found requirements.txt file . Installing python requirements"

      echo ""
      echo "---------------------------Starting to Install Requirements---------------------------"
      if [[ -v ARTIFACTORY_URL ]]; then
        if ! pip install --index-url="$ARTIFACTORY_URL" -r "$REPOSITORY_DIR"/requirements.txt --root-user-action=ignore; then
          log "ERROR" "FINISHED" "Processing Failed!"
          echo "Processing Failed!"
        fi
      else
        if ! pip install -r "$REPOSITORY_DIR"/requirements.txt --root-user-action=ignore; then
          log "ERROR" "FINISHED" "Processing Failed!"
          echo "Processing Failed!"
        fi
      fi
      echo "---------------------------Finished Installing Requirements---------------------------"
      echo ""
    fi
#  else
#    log "info" "PREPARING" "requirements.txt not found --> Continuing"
  fi

  log "INFO" "PREPARING" "User defined dependencies installed successfully"

  return 0

#  echo
#  echo "Installed software:"
#  python --version
#  pip --version
#
#  echo
#  echo "Installed Python packages:"
#  pip list -l

}

# Array of required environment variables
variables=("BUILD_ID"
"BUILD_TOKEN"
"BUILD_TYPE"
"REPOSITORY_ID"
"BRANCH"
"SCRIPT_PATH"
#"BRANCH_ID"
#"COMMIT_ID"
"JULIA_HOST"
"JULIA_PORT"
"JULIA_URL"
"ACCESS_TOKEN")

# Function to check if environment variables are set
function checkEnvVariables() {
    for var in "$@"; do
        if [[ -v "${!var}" ]]; then
            return 1
        fi
    done
    return 0
}

# Check if any required variables are missing
if ! checkEnvVariables "${variables[@]}"; then
    echo "Missing one or more required environment variables"
    log "ERROR" "PREPARING" "Missing one or more required environment variables"
    exit 1
fi

# Orphea Specific
if [[ -v GOOGLE_CLOUD_CREDENTIALS ]]; then
  echo "$GOOGLE_CLOUD_CREDENTIALS" >>  /root/google_creds.json
fi
# Orphea Specific


case "$1" in
  driver)
    shift 1
    CMD=(
      "$SPARK_HOME/bin/spark-submit"
      --conf "spark.driver.bindAddress=$SPARK_DRIVER_BIND_ADDRESS"
      --deploy-mode client
      "$@"
    )

      # ORPHEA Specific : git clone the the repository based on repository Id
#          env
          if [[ -v REPOSITORY_ID ]]; then
              log "INFO" "PREPARING" "Preparing the setup."

              cd /opt

              # skip below if PREVIEW
              if [ "$BUILD_TYPE" != "PREVIEW" ]; then

                if git clone "${JULIA_URL}julia/${REPOSITORY_ID}/" ; then

                  cd "${REPOSITORY_ID}"
                  git config --global advice.detachedHead false
                  if [[ -v COMMIT_ID ]]; then # if no COMMIT_ID found then use branch name
                    log "INFO" "PREPARING" "COMMIT_ID $COMMIT_ID found using it for checkout"
                    git checkout "${COMMIT_ID}" >> /dev/null
                  else
        #            log "INFO" "PREPARING" "No COMMIT_ID $COMMIT_ID found using branch"
                    git checkout "$BRANCH" >> /dev/null
                    COMMIT_ID=$(git log --format="%H" -n 1)
                    BRANCH_ID=$(git rev-parse "$BRANCH")
                    export COMMIT_ID BRANCH_ID
                  fi
                  log "INFO" "PREPARING" "REPOSITORY Cloned and Checkout successful"
        #          log "INFO" "PREPARING" "Installing user defined dependencies"

                  install_dependencies /opt/"${REPOSITORY_ID}"

  #              else
  #                log "ERROR" "PREPARING" "Error occurred while install Python Requirements"
  #
  #              fi
                else
                  log "ERROR" "PREPARING" "Error occurred while trying to clone"
                fi
              fi
          fi

          cd "$OLDPWD"
          log "INFO" "PREPARING" "Awaiting resource allocation"


    ;;
  executor)
    shift 1
    CMD=(
      ${JAVA_HOME}/bin/java
      -Dfile.encoding=UTF-8
      -Dsun.jnu.encoding=UTF-8
      "${SPARK_EXECUTOR_JAVA_OPTS[@]}"
      -Xms$SPARK_EXECUTOR_MEMORY
      -Xmx$SPARK_EXECUTOR_MEMORY
      -cp "$SPARK_CLASSPATH:$SPARK_DIST_CLASSPATH"
      org.apache.spark.scheduler.cluster.k8s.KubernetesExecutorBackend
      --driver-url $SPARK_DRIVER_URL
      --executor-id $SPARK_EXECUTOR_ID
      --cores $SPARK_EXECUTOR_CORES
      --app-id $SPARK_APPLICATION_ID
      --hostname $SPARK_EXECUTOR_POD_IP
      --resourceProfileId $SPARK_RESOURCE_PROFILE_ID
      --podName $SPARK_EXECUTOR_POD_NAME
    )
    ;;

  *)
    echo "Non-spark-on-k8s command provided, proceeding in pass-through mode..."
    CMD=("$@")
    ;;
esac


# Execute the container CMD under tini for better hygiene
if /usr/bin/tini -s -- "${CMD[@]}"; then
  log "INFO" "FINISHED" "Execution Finished"
  echo "Pod Finished"
else
  echo $?

  if [ -s error.log ]; then
    error_message=$(<error.log)
    echo "Error Message: $error_message"
  fi

  log "ERROR" "FINISHED" "Execution Failed!"
  echo "Pod failed!"
fi