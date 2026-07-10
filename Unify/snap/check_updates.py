import os
import sys
import yaml
import subprocess
import requests
import tarfile
from datetime import datetime
import psutil
import time


# Define colors and formatting
GREEN = '\033[0;32m'  # Green color
RED = '\033[0;31m'  # Red color
BOLD = '\033[1m'  # Bold text
RESET = '\033[0m'  # Reset color and formatting

LOCK_FILE = "/tmp/movetodata_check_updates.lock"


def acquire_lock():
    """Acquire the lock if not already taken, or if the process with the lock has died."""
    if os.path.exists(LOCK_FILE):
        with open(LOCK_FILE, 'r') as lock_file:
            pid = int(lock_file.read().strip())
            # Check if the process is still running
            if psutil.pid_exists(pid):
                print(f"{datetime.now()} : {RED}[ERROR]{RESET} : Script is already running with PID {pid}.")
                sys.exit(1)
            else:
                # Process is not running, so remove the stale lock file
                print(f"{datetime.now()} : {RED}[WARNING]{RESET} : Found stale lock file with PID {pid}. Removing it.")
                os.remove(LOCK_FILE)

    # Create a new lock file with the current PID
    with open(LOCK_FILE, 'w') as lock_file:
        lock_file.write(str(os.getpid()))


def release_lock():
    """Release the lock by deleting the lock file."""
    if os.path.exists(LOCK_FILE):
        os.remove(LOCK_FILE)


# Load configuration from YAML file
def load_config(config_file):
    with open(config_file, 'r') as file:
        return yaml.safe_load(file)


# Function to check the validity of the image name
def check_image_name():
    for image in valid_images:
        if IMAGE == image:
            print(f"{datetime.now()} : {GREEN}[INFO]{RESET} : Updating {BOLD}{IMAGE}{RESET}")
            return
    print(f"{datetime.now()} : {RED}[ERROR]{RESET} : Image '{BOLD}{IMAGE}{RESET}' name not valid.")
    exit(1)


# Function to load the image
def load_image():
    tar_file_path = os.path.join(MOVETODATA_IMAGE_DIR, f"{IMAGE}.tar")
    if not os.path.isfile(tar_file_path):
        print(f"{datetime.now()} : {RED}[ERROR]{RESET} : Tar file '{tar_file_path}' not found!")
        exit(1)

    os.chdir(MOVETODATA_IMAGE_DIR)
    try:
        # Run the ctr import command and capture the output
        process = subprocess.Popen(
            f"ctr -n k8s.io images import {tar_file_path} | grep unpacking | awk '{{print $2}}' | awk -F: '{{print $2}}'",
            shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE
        )
        stdout, stderr = process.communicate()

        # Check if the command was successful
        if process.returncode != 0:
            print(f"{datetime.now()} : {RED}[ERROR]{RESET} : Failed to import image. Error: {stderr.decode().strip()}")
            exit(1)

        IMAGE_VERSION = stdout.decode().strip()
        print(
            f"{datetime.now()} : {GREEN}[INFO]{RESET} : {BOLD}{IMAGE}{RESET} image loaded to ctr with version {IMAGE_VERSION}")

    except Exception as e:
        print(f"{datetime.now()} : {RED}[ERROR]{RESET} : An error occurred: {str(e)}")
        exit(1)


# Function to update Helm values
def update_helm_values():
    helm_values_file = os.path.join(HELM_DIR, f"charts/{HELM_CHART}/{HELM_VALUES}")
    if not os.path.isfile(helm_values_file):
        print(f"{datetime.now()} : {RED}[ERROR]{RESET} : {helm_values_file}{RESET} file not found!")
        exit(1)

    updated_content = []
    with open(helm_values_file, 'r') as file:
        for line in file:
            if f"  {IMAGE}:" in line:
                updated_content.append(f"  {IMAGE}: {IMAGE_VERSION}\n")
            else:
                updated_content.append(line)

    with open(helm_values_file, 'w') as file:
        file.writelines(updated_content)

    print(
        f"{datetime.now()} : {GREEN}[INFO]{RESET} : {BOLD}{IMAGE}{RESET} Kubernetes updated to version {IMAGE_VERSION}")


# Function to check for updates
def check_updates():
    helm_values_file = os.path.join(HELM_DIR, f"charts/{HELM_CHART}/{HELM_VALUES}")
    payload = {}

    for component in valid_images:
        with open(helm_values_file, 'r') as file:
            current_version = [line.split()[1] for line in file if line.startswith(f"  {component}:")]
        if current_version:
            payload[component] = current_version[0]
        else:
            payload[component] = None

    response = requests.get(f'{UPDATE_HOST}/api/deployments/configuration/check/target/state/{DEPLOYMENT_ID}',
                            headers={'Authorization': f'Bearer {TOKEN}'})

    if response.status_code != 200:
        print(
            f"{datetime.now()} : {RED}[ERROR]{RESET} : API call error. Status code: {response.status_code}, Response: {response.text}")
        return

    response_data = response.json()

    keys_to_remove = ['id', 'state', 'deployedAt']
    for key in keys_to_remove:
        response_data.pop(key, None)

    for component, version in response_data.items():
        if version and version != payload.get(component):
            print(f"{datetime.now()} : {GREEN}[INFO]{RESET} : Component to update : {BOLD}{component}{RESET}")
            global IMAGE, IMAGE_VERSION
            IMAGE = component
            IMAGE_VERSION = version
            check_image_name()
            download_image()
            load_image()
            update_helm_values()
            helm_upgrade()
            change_active_state()
        else:
            print(f"{datetime.now()} : {GREEN}[INFO]{RESET} : Component {BOLD}{component}{RESET} is up to date.")


# Function to change the active state
def change_active_state():
    print(f"{datetime.now()} : {GREEN}[INFO]{RESET} : {BOLD}{IMAGE}{RESET} Updating active state.")

    helm_values_file = os.path.join(HELM_DIR, f"charts/{HELM_CHART}/{HELM_VALUES}")
    payload = {}

    for component in valid_images:
        with open(helm_values_file, 'r') as file:
            current_version = [line.split()[1] for line in file if line.startswith(f"  {component}:")]
        if current_version:
            payload[component] = current_version[0]
        else:
            payload[component] = None

    response = requests.put(f'{UPDATE_HOST}/api/deployments/configuration/state/active/{DEPLOYMENT_ID}',
                            headers={'Authorization': f'Bearer {TOKEN}', 'Content-Type': 'application/json'},
                            json=payload)

    if response.status_code != 200:
        print(
            f"{datetime.now()} : {RED}[ERROR]{RESET} : API call error. Status code: {response.status_code}, Response: {response.text}")
        return

    print(f"{datetime.now()} : {GREEN}[INFO]{RESET} : {BOLD}{IMAGE}{RESET} successfully updated active state.")


# Function to download the image

def download_image():
    print(f"{datetime.now()} : [INFO] : {IMAGE} downloading latest tag")

    DOWNLOAD_IMAGE = IMAGE.replace("movetodataDocs", "movetodata-docs").replace("sparkHistoryServer", "spark-history-server")
    image_path = os.path.join(MOVETODATA_IMAGE_DIR, f"{IMAGE}.tar")

    retries = 3
    for attempt in range(retries):
        try:
            response = requests.get(f'{UPDATE_HOST}/api/artifact/downloadByTriggerName/{DOWNLOAD_IMAGE}',
                                    headers={'Authorization': f'Bearer {TOKEN}'}, stream=True, timeout=(10, 300))

            if response.status_code != 200:
                print(f"{datetime.now()} : [ERROR] : Failed to download {IMAGE}. HTTP Status Code: {response.status_code}")
                return

            # If the tar file exists from a previous failed download, remove it.
            if os.path.exists(image_path):
                os.remove(image_path)

            with open(image_path, 'wb') as file:
                for chunk in response.iter_content(chunk_size=8192):
                    if chunk:
                        file.write(chunk)

            if not tarfile.is_tarfile(image_path):
                print(f"{datetime.now()} : [ERROR] : {IMAGE} is not a valid tar file.")
                os.remove(image_path)
                return

            print(f"{datetime.now()} : [INFO] : {IMAGE} successfully downloaded and verified as a tar file.")
            return  # Exit if successful

        except requests.exceptions.RequestException as e:
            print(f"{datetime.now()} : [ERROR] : An error occurred while downloading {IMAGE}: {str(e)}")
            if os.path.exists(image_path):
                os.remove(image_path)
            print(f"{datetime.now()} : [INFO] : Retrying download ({attempt+1}/{retries})...")

        time.sleep(5)  # Wait 5 seconds before retrying

    print(f"{datetime.now()} : [ERROR] : Failed to download {IMAGE} after {retries} attempts.")



# Function to run Helm upgrade
def helm_upgrade():
    os.chdir(HELM_DIR)
    subprocess.run(f"helm upgrade movetodata charts/{HELM_CHART} -f charts/{HELM_CHART}/{HELM_VALUES}", shell=True)


# Main function
def main():
    acquire_lock()

    script_directory = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_directory)

    check_updates()

    release_lock()


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python script.py <config.yaml> [helm_values.yaml]")
        sys.exit(1)

    # Load configuration from the first argument
    config_file = sys.argv[1]
    config = load_config(config_file)

    # Override HELM_VALUES if passed as the second argument
    if len(sys.argv) > 2:
        HELM_VALUES = sys.argv[2]
    else:
        HELM_VALUES = config["HELM_VALUES"]

    UPDATE_HOST = config["UPDATE_HOST"]
    BUNDLE_DIR = config["BUNDLE_DIR"]
    HELM_CHART = config["HELM_CHART"]
    TOKEN = config["TOKEN"]
    DEPLOYMENT_ID = config["DEPLOYMENT_ID"]
    valid_images = config["valid_images"]

    HELM_DIR = os.path.join(BUNDLE_DIR, "deployments/configurations/helm")
    MOVETODATA_IMAGE_DIR = os.path.join(BUNDLE_DIR, "images/movetodata")

    main()
