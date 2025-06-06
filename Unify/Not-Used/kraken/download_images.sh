#!/bin/bash

source env.sh

if [ ! -d bundle/$TAG/images/ ]; then
    mkdir -p bundle/$TAG/images/
fi

# Download images
for image_name in $(echo $UPDATED_IMAGES); do

    continue

    image_tag="$ARTIFACTORY_PATH/$image_name:$TAG"

    echo "Downloading image : $image_tag"
    docker pull --platform linux/amd64 $image_tag >/dev/null
    docker save --output bundle/$TAG/images/$image_name.tar $image_tag >/dev/null
done

# Bundle and ship
if [ -f "update_$TAG.tar" ]; then
    rm "update_$TAG.tar"
fi

if [ -f "update_$TAG.tar.gz" ]; then
    rm "update_$TAG.tar.gz"
fi

cp env.sh update_bosler.sh bundle/$TAG

tar cf "update_$TAG.tar" bundle
gzip "update_$TAG.tar"

echo ""
echo "**** : Update Bundle created successfully : ****"
echo ""
echo "Note : Copy to the installation server : (e.g. gcloud compute scp "update_bosler_$TAG.tar.gz" <server>:~/ ) "
echo ""
echo "Login to server"
echo ""
echo "Then Run : sudo tar -C /bosler -xf update_bosler_$TAG.tar.gz && sudo /bosler/bundle/$TAG/update_bosler.sh"
echo ""
