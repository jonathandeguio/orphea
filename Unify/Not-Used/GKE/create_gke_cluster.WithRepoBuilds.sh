#!/usr/bin/env zsh

# Build the GKE cluster
# https://cloud.google.com/kubernetes-engine/docs/quickstart
#
# Pre-Reqs:
# - An account in the appropriate Directory (Google Workspace in Orphea's case).
# - Google Cloud SDK must be installed on local machine or run within the 
#   Google Cloud Shell.  Visit https://cloud.google.com/sdk/docs/install
# - The Kubernetes Kubectl command-line tool needs to be installed on the 
#   computer (this is installed by default in Google Cloud Shell)  
#   https://kubernetes.io/docs/tasks/tools/install-kubectl-macos/.
#   Kubectl is also installed by default with docker desktop 
#   Visit https://www.docker.com/products/docker-desktop.
#

# get environment variables
source env.sh

if ! gcloud --version >/dev/null ; then
    echo "Google SDK gcloud utility not usable!" ; return 1
fi
ACTIVE_ID=`gcloud auth list --filter=status:ACTIVE --format="value(account)"`
PROMPT_MSG="Active gcloud ID is $ACTIVE_ID. Do you wish to proceed? (Y|n) > "
read "VARIN?$PROMPT_MSG"
if [[ "$VARIN" != [Yy] ]] ; then
    print \n exiting... ; return 1
fi

# enable apis
gcloud services enable compute.googleapis.com \
    artifactregistry.googleapis.com \
    cloudbuild.googleapis.com \
    storage.googleapis.com \
    run.googleapis.com


gcloud config set project $PROJECT_ID
gcloud config set compute/region $COMPUTE_REGION
gcloud config set compute/zone $COMPUTE_ZONE

gcloud container clusters create $CLUSTER_NAME --num-nodes=2 --enable-autoupgrade \
     --metadata disable-legacy-endpoints=true -m $MACHINE_TYPE \
     --enable-autoscaling --min-nodes 1 --max-nodes 3

# This command configures kubectl to use the cluster you created.
gcloud container clusters get-credentials $CLUSTER_NAME

echo "cluster info"
kubectl cluster-info

# Reserve a static IP
gcloud compute addresses create $CLUSTER_NAME --global
#gcloud compute addresses create ${CLUSTER_NAME}-kepler --global
#gcloud compute addresses create ${CLUSTER_NAME}-history --global

# Create global pre-shared SSL certificate
CERTIFICATE_NAME="orphea-preshared-cert"
gcloud compute ssl-certificates create $CERTIFICATE_NAME \
    --certificate $CERT_FILE_PATH \
    --private-key $KEY_FILE_PATH 

gcloud services enable artifactregistry.googleapis.com
gcloud artifacts repositories create $LOCALREPO \
    --repository-format=Docker \
    --location="$COMPUTE_REGION" \
    --description="$CLUSTER_NAME Local Docker Repo" \
    --async

### IP WHITELISTING ###
# ALL WORKING - BUT TEMPORARILY UNUSED
#
## create security policy
## 
WHITELISTFILE="../whitelist.${CLUSTER_NAME}.txt"
echo "0.0.0.0/0" >> $WHITELISTFILE 
gcloud compute security-policies create $CLUSTER_NAME \
    --description "Cloud Armor Whitelist Security Policy"
gcloud compute security-policies rules update 2147483647 \
    --security-policy $CLUSTER_NAME \
    --action "deny-403"
if test -f $WHITELISTFILE ; then
    WHITELIST=$(paste -s -d, $WHITELISTFILE)
    gcloud compute security-policies rules create 1000 \
        --security-policy $CLUSTER_NAME \
        --description "allow traffic from whitelist file" \
        --src-ip-ranges "${WHITELIST}" \
        --action "allow"
else
        echo "Error: whitelist file not found!"
fi

## Create a service account
gcloud iam service-accounts create github-actions-policy-updater \
    --description="Github Actions Security Policy Updater" \
    --display-name="github-actions-policy-updater"
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:github-actions-policy-updater@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/compute.orgSecurityPolicyAdmin"
# Generate a base64 encoded key for import into Github Secrets -- DO NOT STORE KEY IN THE REPO!!
 gcloud iam service-accounts keys create /tmp/key-file.json \
    --iam-account=github-actions-policy-updater@${PROJECT_ID}.iam.gserviceaccount.com
# base64 encode before importing as a Github secret.
base64 /tmp/key-file.json > /tmp/key-file.json.base64


# Cloud Build Service Account
# https://cloud.google.com/build/docs/automating-builds/create-manage-triggers
# https://cloud.google.com/sdk/gcloud/reference/beta/builds/triggers/create/github

gcloud iam service-accounts create orpheabuilds \
    --description="Orphea Cloud Build SA" \
    --display-name="Orphea Cloud Build SA"
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:orpheabuilds@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/cloudbuild.serviceAgent"
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:orpheabuilds@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/logging.logWriter"
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:orpheabuilds@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/artifactregistry.writer"

# cloud storage
gcloud iam service-accounts create google-storage-sa \
    --description="Orphea Cloud Storage SA" \
    --display-name="Orphea Cloud Storage SA"

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:google-storage-sa@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/storage.objectCreator"
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:google-storage-sa@${PROJECT_ID}.iam.gserviceaccount.com" \    
    --role="roles/storage.objectViewer"
    

if [ ! -f /tmp/google-storage-sa-key-file-${PROJECT_ID}.json.base64 ]; then
    gcloud iam service-accounts keys create /tmp/google-storage-sa-key-file-${PROJECT_ID}.json --iam-account=google-storage-sa@${PROJECT_ID}.iam.gserviceaccount.com

    base64 -i /tmp/google-storage-sa-key-file-${PROJECT_ID}.json -o /tmp/google-storage-sa-key-file-${PROJECT_ID}.json.base64
fi

## configure auth
#gcloud auth configure-docker ${COMPUTE_REGION}-docker.pkg.dev

gcloud beta artifacts repositories create $LOCALREPO \
    --repository-format=docker --location=$COMPUTE_REGION --description="Docker Repository"

echo "please run through the Connect Repository procedure within Triggers in GCP"
echo "https://console.cloud.google.com/cloud-build/triggers/connect?authuser=4&project=$PROJECT_ID"
/usr/bin/open https://console.cloud.google.com/cloud-build/triggers/connect?authuser=4&project=$PROJECT_ID

for i in $(echo $ACTIVE_REPOS); do
    echo "Creating trigger for $i"
    gcloud beta builds triggers create github --name="${i}" \
        --service-account="projects/${PROJECT_ID}/serviceAccounts/orpheabuilds@${PROJECT_ID}.iam.gserviceaccount.com" \
        --repo-owner="Orphea-io" \
        --repo-name="${i}" \
        --branch-pattern="^main$" \
        --build-config="cloudbuild.yaml"
done
