#!/usr/bin/env /bin/bash

# this script creates and downloads new creds for the compute engine default service account.
# alternatively you can export the creds in JSON format from IAM in the GCP console.

if ! which gcloud && gcloud --version ; then
    echo "gcloud error"
    return 
fi

SVCACCNT=$(gcloud iam service-accounts list | awk '/Compute Engine default service account/ {print $6}')
KEYNM=$(gcloud iam service-accounts keys list --iam-account=$SVCACCNT | tail -1 | awk '{print $1}')

echo "CAUTION:  DO NOT STORE CREDS IN GIT/GITHUB!!"

#gcloud beta iam service-accounts keys get-public-key $KEYNM --iam-account=$SVCACCNT --output-file=/tmp/public_key.pem
gcloud iam service-accounts keys create /tmp/cred.json --iam-account=$SVCACCNT
export GOOGLE_APPLICATION_CREDENTIALS=/tmp/cred.json

