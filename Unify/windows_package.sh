#!/bin/bash

# Base path
BASE_PATH="/Users/commonaccount/Documents/Code/MoveToData"

# Function to copy directories or files
copy_resource() {
    local source_path=$1
    local dest_path=$2
    local resource_name=$3

    echo "$(date): Copying $resource_name"
    if [ -d "$dest_path" ]; then
        rm -rf "$dest_path"
    fi

    if [ -d "$source_path" ]; then
        cp -r "$source_path" "$dest_path"
    else
        cp "$source_path" "$dest_path"
    fi
}

# Function to build frontend
build_frontend() {
    echo "$(date): Building frontend"
    cd "$BASE_PATH/frontend" || exit
    git pull
    [ -d build ] && rm -rf build
    yarn build
}

# Function to build boson
build_boson() {
    export PATH=/Users/commonaccount/Library/Java/JavaVirtualMachines/azul-11.0.13/Contents/Home/bin:$PATH

    echo "$(date): Building boson"
    cd "$BASE_PATH/boson" || exit
    git pull
    [ -d build ] && rm -rf build
    ./gradlew build --stacktrace -x test
}

# Function to build docs
build_docs() {
    echo "$(date): Building docs"
    cd "$BASE_PATH/movetodata-docs2/movetodata-docs" || exit
    git pull
    [ -d build ] && rm -rf build
    yarn build
}

# Define source paths
FRONTEND_SOURCE="$BASE_PATH/frontend/build/"
BOSON_SOURCE="$BASE_PATH/boson/build/libs/boson-0.0.1-SNAPSHOT.jar"
DOCS_SOURCE="$BASE_PATH/movetodata-docs2/movetodata-docs/build/"

# Define destination paths
FRONTEND_DEST="$BASE_PATH/Unify/windows/app/frontend"
BOSON_DEST="$BASE_PATH/Unify/windows/app/boson/boson-0.0.1-SNAPSHOT.jar"
DOCS_DEST="$BASE_PATH/Unify/windows/app/movetodata-docs"

# Function to handle build actions
build_all() {
    build_frontend
    build_boson
    # build_docs
}

# Function to handle copy actions
copy_all() {
    copy_resource "$FRONTEND_SOURCE" "$FRONTEND_DEST" "frontend"
    copy_resource "$BOSON_SOURCE" "$BOSON_DEST" "boson"
    # copy_resource "$DOCS_SOURCE" "$DOCS_DEST" "docs"
}

# Function to handle specific copy actions
copy_specific() {
    local resource=$1
    case $resource in
    frontend)
        copy_resource "$FRONTEND_SOURCE" "$FRONTEND_DEST" "frontend"
        ;;
    boson)
        copy_resource "$BOSON_SOURCE" "$BOSON_DEST" "boson"
        ;;
    docs)
        copy_resource "$DOCS_SOURCE" "$DOCS_DEST" "docs"
        ;;
    *)
        echo "Invalid resource: $resource"
        exit 1
        ;;
    esac
}

# Function to handle specific build actions
build_specific() {
    local resource=$1
    case $resource in
    frontend)
        build_frontend
        ;;
    boson)
        build_boson
        ;;
    docs)
        build_docs
        ;;
    *)
        echo "Invalid resource: $resource"
        exit 1
        ;;
    esac
}

# Check command-line arguments
if [ $# -lt 2 ]; then
    echo "Usage: $0 {buildOnly|copyOnly|all} {frontend|boson|docs|all}"
    exit 1
fi

action=$1
resource=$2

case $action in
buildOnly)
    if [ "$resource" == "all" ]; then
        build_all
    else
        build_specific "$resource"
    fi
    ;;
copyOnly)
    if [ "$resource" == "all" ]; then
        copy_all
    else
        copy_specific "$resource"
    fi
    ;;
all)
    if [ "$resource" == "all" ]; then
        build_all
        copy_all
    else
        build_specific "$resource"
        copy_specific "$resource"
    fi
    ;;
*)
    echo "Invalid action: $action"
    echo "Usage: $0 {buildOnly|copyOnly|all} {frontend|boson|docs|all}"
    exit 1
    ;;
esac

# Remove old zip file if exists
cd "$BASE_PATH/Unify" || exit
ZIP_FILE="SmartViz-windows.zip"
if [ -f "$ZIP_FILE" ]; then
    rm "$ZIP_FILE"
fi

# Create new zip file
zip -vr "$ZIP_FILE" windows/ -x "*.DS_Store"
