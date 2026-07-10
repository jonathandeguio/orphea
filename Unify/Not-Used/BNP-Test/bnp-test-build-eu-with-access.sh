#!/bin/bash

PROJECT_ID=movetodata-foo
COMPUTE_REGION=europe-west1
COMPUTE_ZONE=europe-west1-b

NETWORK_TAG="bnp-test-eu"

INSTANCE_TAG="bnp-instance-with-access"
DISK_TAG="bnp-disk-with-access"

# gcloud compute networks create $NETWORK_TAG \
# --project=$PROJECT_ID \
# --subnet-mode=custom --mtu=1460 --bgp-routing-mode=regional

# gcloud compute networks subnets create $NETWORK_TAG --project=$PROJECT_ID --range=182.168.200.0/24 --network=$NETWORK_TAG --region=$COMPUTE_REGION

# gcloud compute firewall-rules create $NETWORK_TAG-allow-ssh-eu --project=$PROJECT_ID \
# --network=projects/$PROJECT_ID/global/networks/$NETWORK_TAG \
# --description=Allows\ TCP\ connections\ from\ any\ source\ to\ any\ instance\ on\ the\ network\ using\ port\ 22. \
# --direction=INGRESS --priority=65534 \
# --source-ranges=0.0.0.0/0 --action=ALLOW --rules=tcp:22

# gcloud compute --project=$PROJECT_ID firewall-rules create $NETWORK_TAG-deny \
# --description="deny all outgoing" \
# --direction=EGRESS --priority=1000 \
# --network=$NETWORK_TAG --action=DENY \
# --rules=all --target-tags=$NETWORK_TAG

# 4 CPU / 16GB RAM Standard VM with Standard Disks (100GB Boot, 1000GB External)
# Nested Virtualization enabled
# Network tag $NETWORK_TAG

gcloud compute instances create $INSTANCE_TAG --project=$PROJECT_ID \
--zone=$COMPUTE_ZONE \
--machine-type=n1-standard-2 \
--enable-nested-virtualization \
--service-account=735596495996-compute@developer.gserviceaccount.com \
--scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/trace.append \
--create-disk=auto-delete=yes,boot=yes,device-name=instance-1,image=projects/centos-cloud/global/images/centos-7-v20211214,mode=rw,size=100,type=projects/movetodata-foo/zones/$COMPUTE_ZONE/diskTypes/pd-standard \
--create-disk=auto-delete=yes,device-name=$DISK_TAG,mode=rw,name=$DISK_TAG,size=1000,type=projects/movetodata-foo/zones/$COMPUTE_ZONE/diskTypes/pd-standard \
--no-shielded-secure-boot --shielded-vtpm --shielded-integrity-monitoring \
--reservation-affinity=any

# gcloud compute instances stop bnp-instance-1
# gcloud compute instances start bnp-instance-1
# gcloud compute instances delete bnp-instance-1

# Creating and mounting the external disk:
# https://cloud.google.com/compute/docs/disks/add-persistent-disk?hl=en
# e.g.
# gcloud beta compute ssh --zone "$COMPUTE_ZONE" "bnp-instance-1"  --tunnel-through-iap --project "movetodata-foo"
# sudo lsblk
# sudo mkfs.ext4 -m 0 -E lazy_itable_init=0,lazy_journal_init=0,discard /dev/sdb
# sudo mkdir -p /mnt/data
# sudo mount -o discard,defaults /dev/sdb /mnt/data
# df -h
