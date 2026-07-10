#!/bin/bash

# Define colors and formatting
GREEN='\033[0;32m' # Green color
RED='\033[0;31m'   # Red color
BOLD='\033[1m'     # Bold text
RESET='\033[0m'    # Reset color and formatting

# Define variables
IMAGE=$1
IMAGE_VERSION=$2
BUNDLE_DIR="/movetodata/bundle"
HELM_DIR="$BUNDLE_DIR/deployments/configurations/helm"
MOVETODATA_IMAGE_DIR="$BUNDLE_DIR/images/movetodata"
HELM_CHART="movetodata-gke"
HELM_VALUES="demoCluster.yaml"

# Array of valid image names
valid_images=(
    "boson"
    "frontend"
    "funnel"
    "parler"
    "shyne"
    "julia"
    "callisto"
    "movetodataDocs"
    "sparkHistoryServer"
    "connect"
)

# Check if the image name is valid
function check_image_name() {
    for str in "${valid_images[@]}"; do
        if [[ "$IMAGE" == "$str" ]]; then
            echo -e "$(date) : ${GREEN}[INFO]${RESET} : Updating ${BOLD}${IMAGE}${RESET} with version ${BOLD}${IMAGE_VERSION}${RESET}"
            return 0
        fi
    done

    echo -e "$(date) : ${RED}[ERROR]${RESET} : Image '${BOLD}${IMAGE}${RESET}' name not valid."
    exit 1
}

# Load the image if it's not already loaded
function load_image() {
    if ctr -n k8s.io images ls -q | grep -q "$IMAGE" | grep -q "$IMAGE_VERSION"; then
        echo -e "$(date) : ${GREEN}[INFO]${RESET} : ${BOLD}${IMAGE}${RESET} ${BOLD}${IMAGE_VERSION}${RESET} already loaded"
    else
        if [ ! -f "${MOVETODATA_IMAGE_DIR}/${IMAGE}-${IMAGE_VERSION}.tar" ]; then
            echo -e "$(date) : ${RED}[ERROR]${RESET} : ${BOLD}${IMAGE}-${IMAGE_VERSION}.tar${RESET} Image file not found!"
            exit 1
        fi

        cd "$MOVETODATA_IMAGE_DIR" || exit
        ctr -n k8s.io images import "${IMAGE}-${IMAGE_VERSION}.tar"
    fi
}

# Update Helm values if necessary
function update_helm_values() {
    local helm_values_file="${HELM_DIR}/charts/${HELM_CHART}/${HELM_VALUES}"

    if [ ! -f "$helm_values_file" ]; then
        echo -e "$(date) : ${RED}[ERROR]${RESET} : ${helm_values_file}${RESET} file not found!"
        exit 1
    fi

    if grep -q "  ${IMAGE}:" "$helm_values_file" | grep -q "${IMAGE_VERSION}" "$helm_values_file"; then
        echo -e "$(date) : ${RED}[ERROR]${RESET} : ${BOLD}${IMAGE}${RESET} ${BOLD}${IMAGE_VERSION}${RESET} already on platform"
        exit 1
    else
        sed "s/  ${IMAGE}:.*/  ${IMAGE}: ${IMAGE_VERSION}/g" "$helm_values_file" >"$helm_values_file.new"
        mv "$helm_values_file.new" "$helm_values_file"
    fi
}

# Upgrade Helm
function helm_upgrade() {
    cd "$HELM_DIR" || exit
    helm upgrade movetodata charts/"$HELM_CHART" -f "charts/${HELM_CHART}/${HELM_VALUES}"
}

# Display usage information
usage() {
    echo "$0 frontend 0.1.43"
}

# Main function
function main() {
    if [ $# -ne 2 ]; then
        usage
        exit 1
    fi

    check_image_name
    load_image
    update_helm_values
    helm_upgrade
}

main "$@"
