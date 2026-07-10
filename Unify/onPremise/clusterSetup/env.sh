#!/bin/env bash

PROJECT_ID=aprbos23
COMPUTE_REGION=asia-northeast2
COMPUTE_ZONE=asia-northeast2-b
#COMPUTE_REGION=europe-west2
#COMPUTE_ZONE=europe-west2-b

SERVIE_ACCOUNT=891266870742-compute@developer.gserviceaccount.com

NETWORK_TAG="movetodata-test-eu"

INSTANCE_TAG="movetodata-instance-with-access"
DISK_TAG="movetodata-disk-with-access"


export PROJECT_ID COMPUTE_REGION COMPUTE_ZONE NETWORK_TAG INSTANCE_TAG DISK_TAG
