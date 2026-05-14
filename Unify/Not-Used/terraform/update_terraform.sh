!/usr/bin/env /bin/bash

if ! uname | grep "Darwin" ; then
    print "Script for MacOS only!"
    return
fi

TFURL="https://www.terraform.io/downloads"

echo "opening browser at $TFURL"
open $TFURL
read -p "please provide URL to the terraform zip file: " URL
curl -s "${URL}" -o ${HOME}/Downloads/tf.zip
unzip ${HOME}/Downloads/tf.zip
ls ${HOME}/Downloads/terraform >/dev/null && \
    sudo cp ${HOME}/Downloads/terraform /usr/local/bin/ && \
    sudo chown root:wheel /usr/local/bin/terraform && \
    sudo chmod 755 /usr/local/bin/terraform
terraform --version
