#!/bin/bash

# Define colors and formatting
GREEN='\033[0;32m' # Green color
RED='\033[0;31m'   # Red color
BOLD='\033[1m'     # Bold text
RESET='\033[0m'    # Reset color and formatting

# Define variables
IMAGE=$1
IMAGE_VERSION=$2
BUNDLE_DIR="/orphea/bundle"
HELM_DIR="$BUNDLE_DIR/deployments/configurations/helm"
ORPHEA_IMAGE_DIR="$BUNDLE_DIR/images/orphea"
HELM_CHART="orphea-gke"
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
    "orpheaDocs"
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
        if [ ! -f "${ORPHEA_IMAGE_DIR}/${IMAGE}-${IMAGE_VERSION}.tar" ]; then
            echo -e "$(date) : ${RED}[ERROR]${RESET} : ${BOLD}${IMAGE}-${IMAGE_VERSION}.tar${RESET} Image file not found!"
            exit 1
        fi

        cd "$ORPHEA_IMAGE_DIR" || exit
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

# build image
function build_image() {
    if ctr -n k8s.io images ls -q | grep -q "$IMAGE" | grep -q "$IMAGE_VERSION"; then
        echo -e "$(date) : ${GREEN}[INFO]${RESET} : ${BOLD}${IMAGE}${RESET} ${BOLD}${IMAGE_VERSION}${RESET} already loaded"
    else

        cd /orphea/repos/"${IMAGE}"
        git pull
        docker build . --tag orphea.io/"${IMAGE}":"${IMAGE_VERSION}"

        if [ ! -f "${ORPHEA_IMAGE_DIR}/${IMAGE}-${IMAGE_VERSION}.tar" ]; then
            docker save --output "${ORPHEA_IMAGE_DIR}/${IMAGE}-${IMAGE_VERSION}.tar" orphea.io/"${IMAGE}":"${IMAGE_VERSION}"
        fi
    fi
}

function clean_up {

    start_time_clean_up=$(date +%s)

    local default_choice="Y" # Default choice set to Y
    local timeout=5          # Countdown time in seconds

    printf "Do you want to clean up old images? (Y/N) ? \n\n"
    for ((i = $timeout; i > 0; i--)); do
        printf "\r%s" "Build starting in $i seconds...(default Y):"
        read -t 1 -n 1 -s prune_input

        if [ ! -z "$prune_input" ]; then
            prune="$prune_input"
            break
        fi
    done

    # If the user input is empty, set the choice to the default value
    if [ -z "$prune" ]; then
        prune="$default_choice"
    fi

    # Check if the user input is a variant of 'yes'
    if [[ $prune == [yY] || $prune == [yY][eE][sS] ]]; then
        if [ -d "${ORPHEA_IMAGE_DIR}" ]; then
            rm -rf "${ORPHEA_IMAGE_DIR}/*"
        fi

        docker system prune -a -f
    fi

    end_time_clean_up=$(date +%s)
    duration_clean_up=$((end_time_clean_up - start_time_clean_up))

    # Calculate hours, minutes, and remaining seconds
    hours_clean_up=$((duration_clean_up / 3600))
    minutes_clean_up=$(((duration_clean_up % 3600) / 60))
    seconds_clean_up=$((duration_clean_up % 60))

    echo "Clean Up took: ${hours_clean_up}h ${minutes_clean_up}m ${seconds_clean_up}s"

}

# push to azure
function azure_push() {
    docker tag "orphea.io/${IMAGE}:${IMAGE_VERSION}" "orpheadevacr.azurecr.io/${IMAGE}:${IMAGE_VERSION}"

    docker push "orpheadevacr.azurecr.io/${IMAGE}:${IMAGE_VERSION}"
}

# Upgrade Helm
function helm_upgrade() {
    cd "$HELM_DIR" || exit
    helm upgrade orphea charts/"$HELM_CHART" -f "charts/${HELM_CHART}/${HELM_VALUES}"
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

    start_time=$(date +%s) # Get start time in seconds since epoch

    clean_up
    build_image
    # check_image_name
    # load_image
    # update_helm_values
    # helm_upgrade
    azure_push

    end_time=$(date +%s)                # Get end time in seconds since epoch
    duration=$((end_time - start_time)) # Calculate duration in seconds

    # Calculate hours, minutes, and remaining seconds
    hours=$((duration / 3600))
    minutes=$(((duration % 3600) / 60))
    seconds=$((duration % 60))

    echo ""
    echo "Total time taken: ${hours}h ${minutes}m ${seconds}s"
}

main "$@"
