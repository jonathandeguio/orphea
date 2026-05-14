#!/bin/bash

source env.sh

load_images() {
    if ! [ $USER = root ]; then
        echo run this script with sudo
        exit 3
    fi
    # load images
    for image_name in $(echo $UPDATED_IMAGES); do
        ls $BUNDLE_DIR/$TAG/images/$image_name.tar | while read line; do
            sudo docker load -q --input $line
        done
    done

}

change_tags() {
    #image tags
    for image_name in $(echo $UPDATED_IMAGES); do
        sudo docker tag $ARTIFACTORY_PATH/$image_name $CLIENT_TAG/$image_name
    done
}

restart_deployments() {
    for image_name in $(echo $UPDATED_IMAGES); do
        echo "Restarting $image_name"
        kubectl -n orphea scale deploy $image_name --replicas=0 >>/dev/null
        sleep 5
        kubectl -n orphea scale deploy $image_name --replicas=1 >>/dev/null
    done
}

load_images
change_tags
restart_deployments
