#!/usr/bin/env bash

# Constants
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IMAGES_DIR="$SCRIPT_DIR/images"
REPOS_DIR="$SCRIPT_DIR/repos"
IMAGES=("boson:latest" "frontend:latest")
IMAGE_NAMES=("boson" "frontend" "movetodata-docs")
REPOS=("git@github.com:MoveToData-io/frontend.git" "git@github.com:MoveToData-io/boson.git")

# Check if git is installed
check_git() {
  if ! git --version &>/dev/null; then
    echo "Error: git client is not installed; it is essential."
    exit 1
  fi
}

# Generic function to exit with error message
exit_with_error() {
  echo "$1"
  exit 1
}

# Ensure directory exists
ensure_dir() {
  local dir="$1"
  [ ! -d "$dir" ] && mkdir -p "$dir"
}

# Build and save Docker images
build_container_image() {
  ensure_dir "$IMAGES_DIR"

  for i in "${!IMAGES[@]}"; do
    local image_name=${IMAGE_NAMES[$i]}
    local image_tag=${IMAGES[$i]}
    echo "$(date): Building $image_name"
    docker compose build "$image_name" || exit_with_error "Failed to build $image_tag"
    
    local image_file="$IMAGES_DIR/$image_name.tar"
    echo "$(date): Saving $image_name to $image_file"
    [ -f "$image_file" ] && rm "$image_file"
    docker save -o "$image_file" "$image_tag" || exit_with_error "Failed to save $image_tag"
  done
}

# Start, stop, and manage Docker containers
start_containers() {
  docker compose up -d || exit_with_error "Failed to start containers"
}

stop_containers() {
  docker compose down || exit_with_error "Failed to stop containers"
}

restart_containers() {
  stop_containers
  start_containers
}

check_container_status() {
  docker compose ps || exit_with_error "Failed to get container status"
}

# Pull Git repositories
pull_images() {
  echo "$(date): Pulling repositories..."
  ensure_dir "$REPOS_DIR"
  cd "$REPOS_DIR" || exit 1

  for repo in "${REPOS[@]}"; do
    local repo_name=$(basename "$repo" .git)
    echo "$(date): Pulling repository $repo"
    if [ ! -d "$repo_name" ]; then
      git clone "$repo" >/dev/null || exit_with_error "Failed to clone $repo"
    else
      (cd "$repo_name" && git pull >/dev/null) || exit_with_error "Failed to update $repo"
    fi
  done
}

# Import Docker images
import_docker_images() {
  if [ -d "$IMAGES_DIR" ]; then
    echo "$(date): Importing Docker images from $IMAGES_DIR..."
    for image in "$IMAGES_DIR"/*.tar; do
      [ -f "$image" ] && docker load -i "$image" || echo "No Docker image tar files found."
    done
  else
    exit_with_error "Directory $IMAGES_DIR does not exist!"
  fi
}

# Import the database
import_database() {
  echo "Waiting for PostgreSQL to start..."
  until docker exec boson-db pg_isready -U postgres &>/dev/null; do
    sleep 1
  done
  echo "PostgreSQL is up. Creating database..."
  docker exec -it boson-db sh -c "su - postgres -c 'psql -c \"CREATE DATABASE boson;\"'" || echo "Database already exists."
}

# Full installation routine
install() {
  import_docker_images
  start_containers
  import_database
  restart_containers
}

# Build the platform (pull, build, and save Docker images)
build_platform() {
  pull_images
  build_container_image
}

# Main function
main() {
  cd "$SCRIPT_DIR" || exit 1
  check_git

  case "$1" in
    start)    start_containers ;;
    stop)     stop_containers ;;
    restart)  restart_containers ;;
    status)   check_container_status ;;
    build)    build_platform ;;
    install)  install ;;
    *)        echo "Usage: $0 {start|stop|restart|status|build|install}" && exit 1 ;;
  esac
}

main "$@"
