#!/bin/bash
# Variables (modify these as needed)
REDIS_DATA_DIR="/var/lib/redis"
REDIS_USER="redis"
REDIS_PASSWORD=""
# Function to add Redis APT repository
add_redis_repo() {
    echo "[INFO] Adding Redis APT repository..."
    sudo add-apt-repository ppa:redislabs/redis -y
    sudo apt update
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] Redis APT repository added successfully."
    else
        echo "[ERROR] Failed to add Redis APT repository."
        exit 1
    fi
}
# Function to install Redis
install_redis() {
    echo "[INFO] Checking if Redis is installed..."
    if ! dpkg -l | grep -qw redis-server; then
        echo "[INFO] Redis not found, installing..."
        sudo apt update && sudo apt install -y redis-server
        if [ $? -eq 0 ]; then
            echo "[SUCCESS] Redis installed successfully."
        else
            echo "[ERROR] Failed to install Redis."
            exit 1
        fi
    else
        echo "[INFO] Redis already installed."
    fi
}
# Function to set up Redis data directory
setup_redis_data_dir() {
    echo "[INFO] Setting up Redis data directory at $REDIS_DATA_DIR..."
    sudo mkdir -p "$REDIS_DATA_DIR"
    sudo chown -R $REDIS_USER:$REDIS_USER "$REDIS_DATA_DIR"
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] Redis data directory set up successfully."
    else
        echo "[ERROR] Failed to set up Redis data directory."
        exit 1
    fi
}
# Function to configure Redis to use the new data directory
configure_redis() {
    echo "[INFO] Configuring Redis to use the new data directory..."
    sudo sed -i "s|^dir .*|dir $REDIS_DATA_DIR|" /etc/redis/redis.conf
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] Redis configuration updated successfully."
    else
        echo "[ERROR] Failed to update Redis configuration."
        exit 1
    fi
}
# Function to set Redis password
set_redis_password() {
    echo "[INFO] Setting Redis password..."
    sudo sed -i "s/^# requirepass .*/requirepass $REDIS_PASSWORD/" /etc/redis/redis.conf
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] Redis password set successfully."
    else
        echo "[ERROR] Failed to set Redis password."
        exit 1
    fi
}
# Function to start Redis service
start_redis_service() {
    echo "[INFO] Starting Redis service..."
    sudo systemctl start redis-server
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] Redis service started successfully."
    else
        echo "[ERROR] Failed to start Redis service."
        exit 1
    fi
}
# Main execution sequence
echo "[INFO] Starting Redis setup process..."
add_redis_repo
install_redis
setup_redis_data_dir
configure_redis
set_redis_password
start_redis_service
echo "[INFO] Redis setup process completed."