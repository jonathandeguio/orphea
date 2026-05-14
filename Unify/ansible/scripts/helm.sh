!/bin/bash

HELM_VERSION="v3.8.0-rc.1"
HELM_URL="https://get.helm.sh/helm-${HELM_VERSION}-linux-amd64.tar.gz"
HELM_INSTALL_DIR="/usr/bin"
TMP_DIR="/tmp"
HELM_TAR="${TMP_DIR}/helm-${HELM_VERSION}-linux-amd64.tar.gz"

# Function to install Helm
install_helm() {
    echo "Installing Helm $HELM_VERSION..."
    curl -sSL -o $HELM_TAR $HELM_URL
    tar -xzvf $HELM_TAR -C $TMP_DIR
    sudo mv ${TMP_DIR}/linux-amd64/helm $HELM_INSTALL_DIR/helm
    rm -rf ${TMP_DIR}/linux-amd64
    echo "Helm $HELM_VERSION installed successfully."
}

# Check if Helm is installed
if command -v helm >/dev/null 2>&1; then
    # Get installed Helm version
    INSTALLED_VERSION=$(helm version --short | grep -oP 'v[0-9]+\.[0-9]+\.[0-9]+(-rc\.[0-9]+)?')

    if [[ "$INSTALLED_VERSION" == "$HELM_VERSION" ]]; then
        echo "Helm $HELM_VERSION is already installed."
        exit 0
    else
        echo "Helm version $INSTALLED_VERSION is installed but different from the required version $HELM_VERSION."
        echo "Removing the current version and installing the required version..."
        sudo rm -f $HELM_INSTALL_DIR/helm
        install_helm
    fi
else
    echo "Helm is not installed. Installing Helm $HELM_VERSION..."
    install_helm
fi
