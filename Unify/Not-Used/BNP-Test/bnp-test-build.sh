#!/bin/bash

PROJECT_ID=movetodata-foo
COMPUTE_REGION=us-central1
COMPUTE_ZONE=us-central1-c

gcloud compute networks create bnp-test \
--project=$PROJECT_ID \
--subnet-mode=custom --mtu=1460 --bgp-routing-mode=regional

gcloud compute networks subnets create bnp-test --project=$PROJECT_ID \
--range=192.168.200.0/24 --network=bnp-test --region=$COMPUTE_REGION

gcloud compute firewall-rules create bnp-test-allow-ssh --project=$PROJECT_ID \
--network=projects/$PROJECT_ID/global/networks/bnp-test \
--description=Allows\ TCP\ connections\ from\ any\ source\ to\ any\ instance\ on\ the\ network\ using\ port\ 22. \
--direction=INGRESS --priority=65534 \
--source-ranges=0.0.0.0/0 --action=ALLOW --rules=tcp:22

gcloud compute --project=$PROJECT_ID firewall-rules create bnp-test-deny \
--description="deny all outgoing" \
--direction=EGRESS --priority=1000 \
--network=bnp-test --action=DENY \
--rules=all --target-tags=bnp-test

# 4 CPU / 16GB RAM Standard VM with Standard Disks (100GB Boot, 1000GB External)
# Nested Virtualization enabled
# Network tag bnp-test

gcloud compute instances create bnp-instance-1 --project=$PROJECT_ID \
--zone=$COMPUTE_ZONE \
--machine-type=n1-standard-4 \
--enable-nested-virtualization \
--tags=bnp-test \
--network-interface=subnet=bnp-test,no-address \
--service-account=735596495996-compute@developer.gserviceaccount.com \
--scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/trace.append \
--create-disk=auto-delete=yes,boot=yes,device-name=instance-1,image=projects/centos-cloud/global/images/centos-7-v20211214,mode=rw,size=100,type=projects/movetodata-foo/zones/us-central1-c/diskTypes/pd-standard \
--create-disk=auto-delete=yes,device-name=bnp-disk-1,mode=rw,name=bnp-disk-1,size=1000,type=projects/movetodata-foo/zones/us-central1-c/diskTypes/pd-standard \
--no-shielded-secure-boot --shielded-vtpm --shielded-integrity-monitoring \
--reservation-affinity=any

# gcloud compute instances stop bnp-instance-1
# gcloud compute instances start bnp-instance-1
# gcloud compute instances delete bnp-instance-1

# Creating and mounting the external disk:
# https://cloud.google.com/compute/docs/disks/add-persistent-disk?hl=en
# e.g.
# gcloud beta compute ssh --zone "us-central1-c" "bnp-instance-1"  --tunnel-through-iap --project "movetodata-foo"
# sudo lsblk
# sudo mkfs.ext4 -m 0 -E lazy_itable_init=0,lazy_journal_init=0,discard /dev/sdb
# sudo mkdir -p /mnt/data
# sudo mount -o discard,defaults /dev/sdb /mnt/data
# df -h
