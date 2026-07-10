#!/usr/bin/env bash

# Check if git is installed
git --version 2>&1 >/dev/null
GIT_IS_AVAILABLE=$?
if [ $GIT_IS_AVAILABLE -ne 0 ]; then
  echo "Error: git client is not installed; it is essential."
  exit 1
fi

# Function to start containers
start_containers() {
  docker compose up --build -d
}

# Function to stop containers
stop_containers() {
  docker compose down
}

# Function to restart containers
restart_containers() {
  stop_containers
  start_containers
}

# Function to builds containers
restart_containers() {
  
  stop_containers
  start_containers
}

# Function to check container status
check_container_status() {
  docker compose ps
}

pull_images() {
  # Create repo directory if not exists
  if [ ! -d repos ]; then
    mkdir repos
  fi

  # Clone or pull snap-ui repository
  cd repos
  if [ ! -d snap-ui ]; then
    git clone git@github.com:MoveToData-io/snap-ui.git >/dev/null
  else
    cd snap-ui
    git pull >/dev/null
    cd ..
  fi

  # Clone or pull snap repository
  if [ ! -d snap ]; then
    git clone git@github.com:MoveToData-io/snap.git >/dev/null
  else
    cd snap
    git pull >/dev/null
    cd ..
  fi
  cd ..

  # Pull latest changes
  git pull >/dev/null

}

# Main function
main() {

  # Get the directory of the script
  script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

  # Change to the script's directory
  cd "$script_dir"

  # Now you can run other commands relative to the script's directory
  echo "Current working directory is now: $script_dir"

	
  pull_images

  # Check the command argument and perform actions accordingly
  case "$1" in
    start)
      start_containers
      ;;
    stop)
      stop_containers
      ;;
    restart)
      restart_containers
      ;;
    status)
      check_container_status
      ;;
    *)
      echo "Usage: $0 {start|stop|restart|status}"
      exit 1
  esac
}

# Call the main function with provided arguments
main "$@"

