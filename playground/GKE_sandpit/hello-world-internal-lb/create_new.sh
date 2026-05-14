#!/usr/bin/env zsh

# https://cloud.google.com/load-balancing/docs/https/setting-up-reg-ext-https-lb#gcloud
# - Network. The network is a custom-mode VPC network named lb-network.
# - Subnet for backends. A subnet named backend-subnet in the us-west1 region uses 10.1.2.0/24 for its primary IP range.
# - Subnet for proxies. A subnet named proxy-only-subnet in the us-west1 region uses 10.129.0.0/23 for its primary IP range.

PROJECT_ID=orphea-test
COMPUTE_REGION=europe-west2
COMPUTE_ZONE=europe-west2-a
CLUSTER_NAME=hello-world
CERTIFICATE_NAME="orphea-preshared-cert"
CERT_FILE_PATH="/Users/jamesmorrish/OneDrive/Jim Documents/Orphea/SECRETS/Orphea.io-ssl-cert/_.orphea.io.crt"
KEY_FILE_PATH="/Users/jamesmorrish/OneDrive/Jim Documents/Orphea/SECRETS/Orphea.io-ssl-cert/myserver.key"
SOURCE_RANGE="0.0.0.0/0"

# BACKEND NAME ???  

gcloud config set project $PROJECT_ID
gcloud config set compute/region $COMPUTE_REGION
gcloud config set compute/zone $COMPUTE_ZONE

gcloud beta compute networks create lb-network --subnet-mode=custom
gcloud beta compute networks subnets create backend-subnet \
    --network=lb-network \
    --range=10.1.2.0/24 \
    --region=$COMPUTE_REGION

gcloud beta compute networks subnets create proxy-only-subnet \
  --purpose=REGIONAL_MANAGED_PROXY \
  --role=ACTIVE \
  --region=$COMPUTE_REGION \
  --network=lb-network \
  --range=10.129.0.0/23

#gcloud beta compute firewall-rules create fw-allow-ssh \
#    --network=lb-network \
#    --action=allow \
#    --direction=ingress \
#    --target-tags=allow-ssh \
#    --rules=tcp:22

gcloud beta compute firewall-rules create fw-allow-health-check \
    --network=lb-network \
    --action=allow \
    --direction=ingress \
    --source-ranges=130.211.0.0/22,35.191.0.0/16 \
    --target-tags=load-balanced-backend \
    --rules=tcp

gcloud beta compute firewall-rules create fw-allow-proxies \
  --network=lb-network \
  --action=allow \
  --direction=ingress \
  --source-ranges=10.129.0.0/23 \
  --target-tags=load-balanced-backend \
  --rules=tcp:80,tcp:443,tcp:8080

# Target back-end services should be deployed here:
# --network=lb-network  <-- the new VPC
# --subnet=backend-subnet

# Create the GKE cluster
gcloud container clusters create $CLUSTER_NAME \
    --num-nodes=3 \
    -m e2-micro \
    --enable-ip-alias \
    --zone=$COMPUTE_ZONE \
    --network=lb-network \
    --subnetwork=backend-subnet

# Connect kubectl to new cluster
gcloud container clusters get-credentials $CLUSTER_NAME

# deploy k8s manifests
kubectl apply -f configuration/hello-world-deployment-1.yaml
kubectl apply -f configuration/hello-world-service-1.yaml
kubectl apply -f configuration/hello-world-deployment-2.yaml
kubectl apply -f configuration/hello-world-service-2.yaml
kubectl apply -f configuration/hello-world-ingress.yaml

# Internal Ingress IP
gcloud beta compute addresses create ${CLUSTER_NAME}-ingress \
    --region=$COMPUTE_REGION \
    --subnet=backend-subnet

# Load-balancer's IP
gcloud beta compute addresses create ${CLUSTER_NAME}-lb \
   --region=$COMPUTE_REGION \
   --network-tier=STANDARD

# forwarding rule's IP address must use the backend-subnet

# HTTP health check
gcloud beta compute health-checks create http ${CLUSTER_NAME}-basic-check \
   --region=$COMPUTE_REGION \
   --request-path='/' \
   --use-serving-port

# Define the backend service
gcloud beta compute backend-services create ${CLUSTER_NAME}-backend-service \
  --load-balancing-scheme=EXTERNAL_MANAGED \
  --protocol=HTTP \
  --port-name=http \
  --health-checks=${CLUSTER_NAME}-basic-check \
  --health-checks-region=$COMPUTE_REGION \
  --region=$COMPUTE_REGION

# Add backends to the backend service
# This should be the GKE NEG?
for i in $(gcloud compute network-endpoint-groups list | awk '/^k8s/ && /'${CLUSTER_NAME}'/ {print $1}')
do
  gcloud beta compute backend-services add-backend ${CLUSTER_NAME}-backend-service \
    --balancing-mode=RATE \
    --region=$COMPUTE_REGION \
    --network-endpoint-group=$i \
    --network-endpoint-group-region=$COMPUTE_REGION
    # --instance-group=${CLUSTER_NAME}-ingress \
    # --instance-group-zone=$COMPUTE_ZONE
done

# Create the URL map
gcloud beta compute url-maps create ${CLUSTER_NAME}-regional-url-map \
  --default-service=${CLUSTER_NAME}-backend-service \
  --region=${COMPUTE_REGION}

# Create the regional certificate
gcloud beta compute ssl-certificates create $CERTIFICATE_NAME \
    --certificate $CERT_FILE_PATH \
    --private-key $KEY_FILE_PATH \
    --region $COMPUTE_REGION

# Use the regional SSL certificate to create a target proxy
gcloud beta compute target-https-proxies create ${CLUSTER_NAME}-proxy \
    --url-map=${CLUSTER_NAME}-regional-url-map \
    --region=$COMPUTE_REGION \
    --ssl-certificates=$CERTIFICATE_NAME

# Create the HTTPS forwarding rule 
gcloud beta compute forwarding-rules create ${CLUSTER_NAME}-forwarding-rule \
    --load-balancing-scheme=EXTERNAL_MANAGED \
    --network-tier=STANDARD \
    --network=lb-network \
    --subnet=backend-subnet \
    --address=10.1.2.99 \
    --ports=443 \
    --region=$COMPUTE_REGION \
    --target-https-proxy=${CLUSTER_NAME}-proxy \
    --target-https-proxy-region=${COMPUTE_REGION}

# The IP address for the domain in DNS points to the external IP of the load balancer

# To enable session affinity
#gcloud beta compute backend-services update ${CLUSTER_NAME}-backend-service \
#    --session-affinity=[GENERATED_COOKIE | HEADER_FIELD | HTTP_COOKIE | CLIENT_IP]
#    --region=$COMPUTE_REGION

