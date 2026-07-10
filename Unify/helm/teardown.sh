#!/usr/bin/env bash

source env-${PROJECT_ID}.sh

kubectl delete all --all -n movetodata
kubectl delete all --all -n default

kubectl -n movetodata delete pvc $(kubectl -n movetodata get pvc | grep -v NAME | awk '{print $1}')

kubectl delete pv $(kubectl get pv | grep -v NAME | awk '{print $1}')

gcloud -q container clusters delete $CLUSTER_NAME

for i in $(echo $ACTIVE_REPOS); do
    echo "Deleting trigger for $i"
    gcloud -q builds triggers delete "${i}"
done

gcloud -q services disable compute.googleapis.com \
    artifactregistry.googleapis.com \
    cloudbuild.googleapis.com \
    storage.googleapis.com \
    run.googleapis.com

# gcloud storage buckets delete gs://$BUCKET_NAME --project=$PROJECT_ID
