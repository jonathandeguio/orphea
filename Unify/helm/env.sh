HELM_CHART=orphea-gke
FQDN=dev.orphea.io
PROJECT_ID=janbos2023
BUCKET_NAME=datasets_collections_$PROJECT_ID
COMPUTE_REGION=europe-west2
COMPUTE_ZONE=europe-west2-b
CLUSTER_NAME=orphea

HELM_VALUES=Cluster-${PROJECT_ID}.yaml

MACHINE_TYPE=e2-highmem-2
LOCALREPO=orphea-cr
REPO_ID=$PROJECT_ID

CERT_FILE_PATH="./ssl-certificates/_.orphea.io.crt"
KEY_FILE_PATH="./ssl-certificates/myserver.key"

ACTIVE_REPOS="funnel parler spark-history-server boson callisto frontend julia orphea-docs shyne"

export HELM_CHART PROJECT_ID BUCKET_NAME COMPUTE_REGION COMPUTE_ZONE CLUSTER_NAME REPO_ID MACHINE_TYPE LOCALREPO CERT_FILE_PATH KEY_FILE_PATH ACTIVE_REPOS

gcloud config set project $PROJECT_ID
