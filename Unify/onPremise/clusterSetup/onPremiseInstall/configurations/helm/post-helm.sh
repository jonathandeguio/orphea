#!/usr/bin/env bash

# get environment variables
source env.sh

# This is to modify HC to check /api/jupyter/api , other GCP healthchecks fails
callisto_hc=$(gcloud compute health-checks list --global|grep callisto|awk '{print $1}')
gcloud compute health-checks update http $callisto_hc \
    --global \
    --check-interval=20s \
    --timeout=15s \
    --request-path="/api/jupyter/api"

## Install Spark and Spark_history
./bosler_spark_env.sh create