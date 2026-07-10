#!/usr/bin/env bash

# get environment variables
if [ $# -eq 0 ]; then
  echo "No arguments supplied. You need to use env-{project}.sh as argument"
fi

source $1

kubectl -n movetodata exec -it $(kubectl -n movetodata get pods | awk '{print $1}' | grep boson-db) -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE kepler;'\""
kubectl -n movetodata exec -it $(kubectl -n movetodata get pods | awk '{print $1}' | grep boson-db) -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE boson;'\""

# This is to modify HC to check /api/jupyter/api , other GCP healthchecks fails
callisto_hc=$(gcloud compute health-checks list --global | grep callisto | awk '{print $1}')
gcloud compute health-checks update http $callisto_hc \
  --global \
  --check-interval=20s \
  --timeout=15s \
  --request-path="/api/jupyter/api"

## Install Spark and Spark_history
./movetodata_spark_env.sh create
