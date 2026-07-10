#!/bin/bash

# source env.sh

PROJECT_ID=gcp-integration-project-382915
COMPUTE_REGION=europe-west2
COMPUTE_ZONE=europe-west2-b

SERVIE_ACCOUNT=435091822251-compute@developer.gserviceaccount.com

NETWORK_TAG="movetodata-test-eu"

INSTANCE_TAG="movetodata-instance-with-access-ubuntu"
DISK_TAG="movetodata-disk-with-access"

IMAGE="projects/debian-cloud/global/images/debian-11-bullseye-v20230411"

export PROJECT_ID COMPUTE_REGION COMPUTE_ZONE NETWORK_TAG INSTANCE_TAG DISK_TAG

gcloud compute instances create $INSTANCE_TAG --project=$PROJECT_ID --zone=$COMPUTE_ZONE --machine-type=e2-medium \
    --network-interface=network-tier=PREMIUM,stack-type=IPV4_ONLY,subnet=default --maintenance-policy=MIGRATE \
    --provisioning-model=STANDARD --service-account=435091822251-compute@developer.gserviceaccount.com \
    --scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/trace.append \
    --tags=http-server,https-server --create-disk=auto-delete=yes,boot=yes,device-name=instance-1,image=projects/ubuntu-os-cloud/global/images/ubuntu-minimal-2004-focal-v20230524,mode=rw,size=100,type=projects/gcp-integration-project-382915/zones/europe-west1-b/diskTypes/pd-balanced \
    --no-shielded-secure-boot --shielded-vtpm --shielded-integrity-monitoring --labels=goog-ec-src=vm_add-gcloud --reservation-affinity=any
