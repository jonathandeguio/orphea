#!/usr/bin/env bash

# Constants
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPOS_DIR="repos"
IMAGES_DIR="images"

# Check if git is installed
if ! git --version &>/dev/null; then
  echo "Error: git client is not installed; it is essential."
  exit 1
fi

# Functions
build_container_image() {
  docker compose build || exit_with_error "Failed to build containers"
}

save_container_image() {
  local image_dir="$IMAGES_DIR"

  if [ ! -d "$image_dir" ]; then
    mkdir "$image_dir"
  fi

  images=("boson:latest" "frontend:latest" "movetodata-docs:latest")
  image_names=("boson" "frontend" "movetodata-docs")

  for i in "${!images[@]}"; do
    image=${image_names[$i]}
    image_tag=${images[$i]}
    echo "$(date): Saving $image"
    if [ -f "$image_dir/$image.tar" ]; then
      rm "$image_dir/$image.tar"
    fi
    docker save -o "$image_dir/$image.tar" "$image_tag" || exit_with_error "Failed to save $image"
  done
}

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

pull_images() {
  echo "$(date) : Pulling images"
  local repos_dir="$REPOS_DIR"

  if [ ! -d "$repos_dir" ]; then
    mkdir "$repos_dir"
  fi
  cd "$repos_dir" || exit 1

  repos=("git@github.com:MoveToData-io/frontend.git" "git@github.com:MoveToData-io/boson.git" "git@github.com:MoveToData-io/movetodata-docs.git")

  for repo in "${repos[@]}"; do
    repo_name=$(basename "$repo" .git)
    echo "$(date) : Pulling image $repo"
    if [ ! -d "$repo_name" ]; then
      git clone "$repo" >/dev/null || exit_with_error "Failed to clone $repo"
    else
      cd "$repo_name" || exit 1
      git pull >/dev/null
      cd ..
    fi
  done

  cd - || exit 1
}

build_platform() {
  # Define image names and tags
  images=("boson:latest" "frontend:latest" "movetodata-docs:latest")
  image_names=("boson" "frontend" "movetodata-docs")

  # Pull images first
  pull_images

  # Build and save each image separately
  for i in "${!images[@]}"; do
    image_tag=${images[$i]}
    image_name=${image_names[$i]}
    echo "$(date): Building image $image_tag"
    docker compose build "$image_name" || exit_with_error "Failed to build $image_tag"
    
    echo "$(date): Saving image $image_tag"
    local image_file="$IMAGES_DIR/$image_name.tar"
    
    if [ -f "$image_file" ]; then
      rm "$image_file"
    fi
    
    docker save -o "$image_file" "$image_tag" || exit_with_error "Failed to save $image_tag"
  done
}

import_docker_images() {
  local image_dir="$IMAGES_DIR"

  if [ -d "$image_dir" ]; then
    echo "$(date): Importing Docker images from $image_dir..."
    for image in "$image_dir"/*.tar; do
      if [ -f "$image" ]; then
        echo "Loading image: $image"
        docker load -i "$image" || exit_with_error "Failed to load $image"
      else
        echo "$(date): No Docker image tar files found in $image_dir"
      fi
    done
  else
    echo "Directory $image_dir does not exist!"
    exit 1
  fi
}

import_database() {
  # Wait for the PostgreSQL server to start
  echo "Waiting for PostgreSQL to start..."
  until docker exec boson-db pg_isready -U postgres; do
    sleep 1
  done

  echo "PostgreSQL is up. Creating database..."

  # Switch to postgres user and create database
  docker exec -it boson-db sh -c "su - postgres -c 'psql -c \"CREATE DATABASE boson;\"'"

  if [ $? -eq 0 ]; then
    echo "Database created successfully."
  else
    echo "Failed to create database."
  fi
}

install() {
  import_docker_images
  start_containers
  import_database
  restart_containers
}

exit_with_error() {
  echo "$1"
  exit 1
}

# Main function
main() {
  cd "$SCRIPT_DIR" || exit 1
  echo "Current working directory is now: $SCRIPT_DIR"

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
  build)
    build_platform
    ;;
  install)
    install
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status|install}"
    exit 1
    ;;
  esac

  # Optional: Clean up the repos folder after running
  rm -rf "$REPOS_DIR"
}

main "$@"
