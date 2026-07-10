#!/bin/bash

# Variables for registry configuration
REGISTRY_URL="snap.movetodata.io:7376"
REGISTRY_USERNAME="robot\$movetodata-main"
REGISTRY_PASSWORD="8oYRda4Yqah5RfkWF6aQpYPGDZN5cLOd"

# Function to install containerd if not installed
is_containerd_installed() {
    if command -v containerd &> /dev/null; then
        echo "Containerd is already installed."
        return 0
    else
        return 1
    fi
}

# Function to clean up multiple config_path entries in config.toml
cleanup_config_path_duplicates() {
    echo "Cleaning up duplicate config_path entries..."
    # Remove all duplicate config_path lines
    sudo sed -i '/config_path/d' /etc/containerd/config.toml
}

# Function to configure containerd to skip certificate verification using config_path
configure_registry_skip_verify() {
    echo "Configuring containerd to skip certificate verification for $REGISTRY_URL..."

    # Create certs.d directory if not exists
    sudo mkdir -p /etc/containerd/certs.d/$REGISTRY_URL

    # Create or overwrite the hosts.toml configuration for the registry
    cat <<EOL | sudo tee /etc/containerd/certs.d/$REGISTRY_URL/hosts.toml > /dev/null
server = "https://$REGISTRY_URL"

[host."https://$REGISTRY_URL"]
  skip_verify = true

[host."https://$REGISTRY_URL".auth]
  username = "$REGISTRY_USERNAME"
  password = "$REGISTRY_PASSWORD"
EOL

    # Add config_path only once
    if ! grep -q 'config_path = "/etc/containerd/certs.d"' /etc/containerd/config.toml; then
        sudo sed -i '/\[plugins."io.containerd.grpc.v1.cri".registry\]/a\ \ \ \ config_path = "/etc/containerd/certs.d"' /etc/containerd/config.toml
    fi

    echo "Registry configuration updated to skip TLS verification."
}

# Function to install containerd if not installed
install_containerd() {
    if is_containerd_installed; then
        echo "Skipping containerd installation."
        return
    fi

    echo "Updating package index..."
    sudo apt-get update

    echo "Installing dependencies..."
    sudo apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release

    echo "Adding Docker GPG key..."
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

    echo "Adding Docker repository..."
    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
      $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    echo "Installing containerd..."
    sudo apt-get update
    sudo apt-get install -y containerd.io

    echo "Generating default containerd configuration..."
    sudo mkdir -p /etc/containerd
    sudo containerd config default | sudo tee /etc/containerd/config.toml

    echo "Starting and enabling containerd service..."
    sudo systemctl enable containerd
    sudo systemctl start containerd
}

# Function to verify installation
verify_containerd() {
    echo "Verifying containerd installation..."
    containerd --version
    if [ $? -eq 0 ]; then
        echo "Containerd installed successfully!"
    else
        echo "Error: Containerd installation failed."
        exit 1
    fi
}

# Function to check containerd service status and start it if necessary
ensure_containerd_running() {
    if systemctl is-active --quiet containerd; then
        echo "Containerd service is already running."
    else
        echo "Starting containerd service..."
        sudo systemctl start containerd
    fi
}

# Main script execution
install_containerd
cleanup_config_path_duplicates  # Clean up duplicates before applying the new config
configure_registry_skip_verify
ensure_containerd_running
verify_containerd

# Verify setup by pulling an image from the registry
echo "Testing image pull from the registry..."
sudo ctr images pull "snap.movetodata.io:7376/movetodata/parler@sha256:6fd62ebdf2338074e4a8688863bafa9f5adcccee20a9dc26be88a692a9e99209"

echo "Containerd installation and registry configuration complete!"
