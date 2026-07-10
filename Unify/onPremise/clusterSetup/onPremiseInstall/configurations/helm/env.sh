HELM_CHART=movetodata-gke
HELM_VALUES=testCluster.yaml
PROJECT_ID=octbos221
BUCKET_NAME=datasets_collections_$PROJECT_ID
COMPUTE_REGION=europe-west1
COMPUTE_ZONE=europe-west1-b
CLUSTER_NAME=movetodata

MACHINE_TYPE=e2-highmem-2
LOCALREPO=movetodata-cr
CERT_FILE_PATH="./ssl-certificates/_.movetodata.io.crt"
KEY_FILE_PATH="./ssl-certificates/myserver.key"

ACTIVE_REPOS="funnel parler spark-history-server boson callisto frontend tycho julia"

export HELM_CHART PROJECT_ID BUCKET_NAME COMPUTE_REGION COMPUTE_ZONE CLUSTER_NAME REPO_ID MACHINE_TYPE LOCALREPO CERT_FILE_PATH KEY_FILE_PATH ACTIVE_REPOS
