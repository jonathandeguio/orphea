#!/usr/bin/env zsh

# Build the GKE cluster
# https://cloud.google.com/kubernetes-engine/docs/quickstart
#
# Pre-Reqs:
# - An account in the appropriate Directory (Google Workspace in MoveToData's case).
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

#PROJECT_ID=movetodata-test
#COMPUTE_REGION=europe-west2
#COMPUTE_ZONE=europe-west2-a
PROJECT_ID=project2-foo
COMPUTE_REGION=us-central1
COMPUTE_ZONE=us-central1-a
CLUSTER_NAME=hello-world

gcloud config set project $PROJECT_ID
gcloud config set compute/region $COMPUTE_REGION
gcloud config set compute/zone $COMPUTE_ZONE

gcloud container clusters create $CLUSTER_NAME --num-nodes=3 --enable-autoupgrade \
     --metadata disable-legacy-endpoints=true -m e2-micro

# This command configures kubectl to use the cluster you created.
gcloud container clusters get-credentials $CLUSTER_NAME
kubectl cluster-info

# create security policy
gcloud compute security-policies create $CLUSTER_NAME \
    --description "Cloud Armor Whitelist Security Policy"
gcloud compute security-policies rules update 2147483647 \
    --security-policy $CLUSTER_NAME \
    --action "deny-403"
if test -f ./whitelist.txt ; then
    WHITELIST=$(paste -s -d, whitelist.txt)
    gcloud compute security-policies rules create 1000 \
        --security-policy $CLUSTER_NAME \
        --description "allow traffic from whitelist.txt" \
        --src-ip-ranges "${WHITELIST}" \
        --action "allow"
    else
        echo "Error: whitelist.txt not found!"
fi

## 'Update rules' as executed from within the Github Action workflow:
#CLUSTER_NAME=hello-world
#WHITELIST=$(paste -s -d, whitelist.txt)
#gcloud compute security-policies rules update 1000 \  
#        --security-policy $CLUSTER_NAME \
#        --description "allow traffic from whitelist.txt" \
#        --src-ip-ranges "${WHITELIST}" \
#        --action "allow"

# Create a service account
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


