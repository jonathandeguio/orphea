#!/usr/bin/env zsh

# Build the GKE cluster
# https://cloud.google.com/kubernetes-engine/docs/quickstart
#
# Pre-Reqs:
# - An account in the appropriate Directory (Google Workspace in Bosler's case).
# - Google Cloud SDK must be installed on local machine or run within the 
#   Google Cloud Shell.  Visit https://cloud.google.com/sdk/docs/install
# - The Kubernetes Kubectl command-line tool needs to be installed on the 
#   computer (this is installed by default in Google Cloud Shell)  
#   https://kubernetes.io/docs/tasks/tools/install-kubectl-macos/.
#   Kubectl is also installed by default with docker desktop 
#   Visit https://www.docker.com/products/docker-desktop.
#

if ! gcloud --version >/dev/null ; then
    echo "Google SDK gcloud utility not usable!" ; return 1
fi
ACTIVE_ID=`gcloud auth list --filter=status:ACTIVE --format="value(account)"`
PROMPT_MSG="Active gcloud ID is $ACTIVE_ID. Do you wish to proceed? (Y|n) > "
read "VARIN?$PROMPT_MSG"
if [[ "$VARIN" != [Yy] ]] ; then
    print \n exiting... ; return 1
fi

PROJECT_ID=bosler-test
COMPUTE_REGION=europe-west2
COMPUTE_ZONE=europe-west2-a
CLUSTER_NAME=hello-world
CERTIFICATE_NAME="bosler-preshared-cert"
CERT_FILE_PATH="/Users/jamesmorrish/OneDrive/Jim Documents/Bosler/SECRETS/Bosler.io-ssl-cert/_.bosler.io.crt"
KEY_FILE_PATH="/Users/jamesmorrish/OneDrive/Jim Documents/Bosler/SECRETS/Bosler.io-ssl-cert/myserver.key"

gcloud config set project $PROJECT_ID
gcloud config set compute/region $COMPUTE_REGION
gcloud config set compute/zone $COMPUTE_ZONE

## REMOVE ME ##
#return 1

#gcloud container clusters create $CLUSTER_NAME --num-nodes=3 --enable-autoupgrade \
#     --metadata disable-legacy-endpoints=true -m e2-micro
#
#gcloud container clusters create CLUSTER_NAME \
#    --cluster-version=VERSION_NUMBER \
#    --enable-ip-alias \
#    --zone=ZONE \
#    --network=NETWORK
#
gcloud container clusters create $CLUSTER_NAME \
    --num-nodes=3 \
    -m e2-micro \
    --enable-ip-alias \
    --zone=$COMPUTE_ZONE
    --network=lb-network \
    --subnetwork=backend-subnet
#    --create-subnetwork="" \
#    --enable-autoupgrade \
#    --cluster-version VERSION

# This command configures kubectl to use the cluster you created.
gcloud container clusters get-credentials $CLUSTER_NAME

# Reserve an internal static IP
gcloud compute addresses create ${CLUSTER_NAME}-ingress \
    --region=$COMPUTE_REGION \
    --network-tier=STANDARD \
    --network=lb_network \
    --subnet=backend-subnet

#gcloud compute addresses describe $CLUSTER_NAME

# Create the regional certificate
gcloud compute ssl-certificates create $CERTIFICATE_NAME \
    --certificate $CERT_FILE_PATH \
    --private-key $KEY_FILE_PATH \
    --region $COMPUTE_REGION

#echo "cluster info"
#kubectl cluster-info
