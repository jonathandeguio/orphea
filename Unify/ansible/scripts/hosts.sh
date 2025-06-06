#!/bin/bash

# Variables (modify these as needed)
HOSTS_FILE="/etc/hosts"
MASTER_IP="172.16.0.160"
MASTER_HOSTNAME="master"
WORKER_IP="172.16.0.163"
WORKER_HOSTNAME="worker"

# Check if the master and worker entries already exist in the /etc/hosts file
grep -q "$MASTER_IP" $HOSTS_FILE
if [ $? -ne 0 ]; then
    echo "[INFO] Adding master entry to $HOSTS_FILE..."
    echo "$MASTER_IP   $MASTER_HOSTNAME" | sudo tee -a $HOSTS_FILE > /dev/null
    echo "[SUCCESS] Master entry added."
else
    echo "[INFO] Master entry already exists in $HOSTS_FILE. Skipping."
fi

grep -q "$WORKER_IP" $HOSTS_FILE
if [ $? -ne 0 ]; then
    echo "[INFO] Adding worker entry to $HOSTS_FILE..."
    echo "$WORKER_IP   $WORKER_HOSTNAME" | sudo tee -a $HOSTS_FILE > /dev/null
    echo "[SUCCESS] Worker entry added."
else
    echo "[INFO] Worker entry already exists in $HOSTS_FILE. Skipping."
fi
