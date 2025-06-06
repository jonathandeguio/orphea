#!/bin/bash

# Variables (modify these as needed)
POSTGRES_DATA_DIR="/var/lib/postgresql/17/main"
POSTGRES_USER="postgres"
POSTGRES_PASSWORD="solaris"
POSTGRES_DB="boson"

# Function to add PostgreSQL APT repository
add_postgres_repo() {
    echo "[INFO] Adding PostgreSQL APT repository..."
    sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt/ $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
    wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
    sudo apt update
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] PostgreSQL APT repository added successfully."
    else
        echo "[ERROR] Failed to add PostgreSQL APT repository."
        exit 1
    fi
}

# Function to install PostgreSQL
install_postgres() {
    echo "[INFO] Checking if PostgreSQL is installed..."
    if ! dpkg -l | grep -qw postgresql; then
        echo "[INFO] PostgreSQL not found, installing..."
        sudo apt update && sudo apt install -y postgresql postgresql-client
        if [ $? -eq 0 ]; then
            echo "[SUCCESS] PostgreSQL installed successfully."
        else
            echo "[ERROR] Failed to install PostgreSQL."
            exit 1
        fi
    else
        echo "[INFO] PostgreSQL already installed."
    fi
}

# Function to set up PostgreSQL data directory
setup_postgres_data_dir() {
    echo "[INFO] Setting up PostgreSQL data directory at $POSTGRES_DATA_DIR..."
    sudo mkdir -p "$POSTGRES_DATA_DIR"
    sudo chown -R postgres:postgres "$POSTGRES_DATA_DIR"
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] PostgreSQL data directory set up successfully."
    else
        echo "[ERROR] Failed to set up PostgreSQL data directory."
        exit 1
    fi
}

# Function to initialize PostgreSQL database
initialize_postgres_db() {
    echo "[INFO] Initializing PostgreSQL database..."
    if [ -d "$POSTGRES_DATA_DIR" ]; then
        echo "[INFO] Cleaning up existing PostgreSQL data directory..."
        sudo rm -rf "$POSTGRES_DATA_DIR"
    fi
    sudo -u postgres /usr/lib/postgresql/$(ls /usr/lib/postgresql)/bin/initdb -D "$POSTGRES_DATA_DIR"
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] PostgreSQL database initialized successfully."
    else
        echo "[ERROR] Failed to initialize PostgreSQL database. Check permissions or other issues."
        exit 1
    fi
}

# Function to configure PostgreSQL to use the new data directory
configure_postgres() {
    echo "[INFO] Configuring PostgreSQL to use the new data directory..."
    sudo sed -i "s|^#data_directory = '.*'|data_directory = '$POSTGRES_DATA_DIR'|" /etc/postgresql/$(ls /etc/postgresql)/main/postgresql.conf
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] PostgreSQL configuration updated successfully."
    else
        echo "[ERROR] Failed to update PostgreSQL configuration."
        exit 1
    fi
    # Restart PostgreSQL service to apply changes
    sudo systemctl restart postgresql
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] PostgreSQL service restarted successfully."
    else
        echo "[ERROR] Failed to restart PostgreSQL service."
        exit 1
    fi
}

# Function to create PostgreSQL user if it doesn't exist
create_postgres_user() {
    echo "[INFO] Creating PostgreSQL user $POSTGRES_USER if it doesn't exist..."
    sudo -u postgres psql -tc "SELECT 1 FROM pg_roles WHERE rolname='$POSTGRES_USER';" | grep -q 1
    if [ $? -ne 0 ]; then
        sudo -u postgres psql -c "CREATE USER $POSTGRES_USER WITH PASSWORD '$POSTGRES_PASSWORD';"
        if [ $? -eq 0 ]; then
            echo "[SUCCESS] PostgreSQL user created successfully."
        else
            echo "[ERROR] Failed to create PostgreSQL user."
            exit 1
        fi
    else
        echo "[INFO] PostgreSQL user already exists."
    fi
}

# Function to create PostgreSQL database if it doesn't exist
create_postgres_db() {
    echo "[INFO] Creating PostgreSQL database $POSTGRES_DB if it doesn't exist..."
    sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = '$POSTGRES_DB';" | grep -q 1
    if [ $? -ne 0 ]; then
        sudo -u postgres psql -c "CREATE DATABASE $POSTGRES_DB OWNER $POSTGRES_USER;"
        if [ $? -eq 0 ]; then
            echo "[SUCCESS] PostgreSQL database created successfully."
        else
            echo "[ERROR] Failed to create PostgreSQL database."
            exit 1
        fi
    else
        echo "[INFO] PostgreSQL database already exists."
    fi
}

# Function to start PostgreSQL service
start_postgres_service() {
    echo "[INFO] Starting PostgreSQL service..."
    sudo systemctl start postgresql
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] PostgreSQL service started successfully."
    else
        echo "[ERROR] Failed to start PostgreSQL service."
        exit 1
    fi
}

# Main execution sequence
echo "[INFO] Starting PostgreSQL setup process..."
add_postgres_repo
install_postgres
setup_postgres_data_dir
initialize_postgres_db
configure_postgres
create_postgres_user
create_postgres_db
start_postgres_service
echo "[INFO] PostgreSQL setup process completed successfully."
