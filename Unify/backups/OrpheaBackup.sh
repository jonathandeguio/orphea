#!/bin/bash

# Function to log messages with timestamp
log_info() {
   echo "$(date) : [INFO] $1"
}

# Function to perform PostgreSQL backup
backup_postgres() {
   log_info "Starting PostgreSQL backup"

   # Run pg_dump inside the pod
   kubectl exec -n "$NAMESPACE" "$POD_NAME" -- pg_dump -U "$DB_USERNAME" -d "$DB_NAME" -F c -f "$BACKUP_TMP_PATH"

   # Copy the backup file from the pod to the local machine
   kubectl cp "$NAMESPACE/$POD_NAME:$BACKUP_TMP_PATH" "$BACKUP_PG_PATH"

   # Remove the backup file from the pod
   kubectl exec -n "$NAMESPACE" "$POD_NAME" -- rm "$BACKUP_TMP_PATH"

   log_info "Finished PostgreSQL backup"
}

# Function to perform data backup
backup_data() {
   log_info "Starting data backup"

   cd "/movetodata/$DEPLOYMENT/" || exit
   tar cfz "$BACKUP_DATA_PATH" localFS git

   log_info "Finished data backup"
}

# Main script starts here

# Check if deployment argument is provided
if [ -z "$1" ]; then
   echo "Usage: $0 <deployment>"
   exit 1
fi

# Define variables
DEPLOYMENT="$1"
NAMESPACE="movetodata-$DEPLOYMENT"
POD_NAME=$(kubectl -n "$NAMESPACE" get pods | awk '{print $1}' | grep boson-db)
DB_USERNAME="postgres"
DB_NAME="boson"
BACKUP_TIMESTAMP=$(date +%Y%m%d%H%M%S)
BACKUP_TMP_PATH="/tmp/$DEPLOYMENT-postgres-backup-$BACKUP_TIMESTAMP.dump"
BACKUP_PATH="/movetodata/$DEPLOYMENT/backups/$BACKUP_TIMESTAMP"
BACKUP_PG_PATH="$BACKUP_PATH/$DEPLOYMENT-postgres-backup-$BACKUP_TIMESTAMP.dump"
BACKUP_DATA_PATH="$BACKUP_PATH/$DEPLOYMENT-data-backup-$BACKUP_TIMESTAMP.tar.gz"

# Create backup directory if it doesn't exist
if [ ! -d "$BACKUP_PATH" ]; then
   mkdir -p "$BACKUP_PATH"
fi

# Perform PostgreSQL and data backups
backup_postgres
backup_data

log_info "Backup completed and saved as $BACKUP_PATH"
