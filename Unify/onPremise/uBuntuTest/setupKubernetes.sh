BUNDLE_DIR=/bosler/bundle

OFFLINE_INSTALL="false"

download_ubuntu_packages() {

    if [ -f /etc/apt/sources.list.d/localKuber.list ]; then
        rm /etc/apt/sources.list.d/localKuber.list
    fi

    sudo install -m 0755 -d /etc/apt/keyrings
    if [ ! -f /etc/apt/keyrings/docker.gpg ]; then
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
        sudo chmod a+r /etc/apt/keyrings/docker.gpg
    fi

    # Step 3 : Use the following command to set up the repository:
    # if [ ! -f /etc/apt/sources.list.d/docker.list ]; then
    #     echo \
    #         "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu focal stable" |
    #         sudo tee /etc/apt/sources.list.d/docker.list >/dev/null
    # fi

    if [ ! -f /etc/apt/keyrings/kubernetes-archive-keyring.gpg ]; then
        curl -fsSLo /etc/apt/kubernetes-archive-keyring.gpg https://dl.k8s.io/apt/doc/apt-key.gpg
    fi

    # step 3: Add the Kubernetes apt repository:
    if [ ! -f /etc/apt/sources.list.d/kubernetes.list ]; then
        echo "deb [signed-by=/etc/apt/kubernetes-archive-keyring.gpg] https://apt.kubernetes.io/ kubernetes-xenial main" | tee /etc/apt/sources.list.d/kubernetes.list
    fi

    apt-get update -qq -y

    apt-get -qq -y install dpkg-dev

    PACKAGE_LIST="dpkg-dev ca-certificates gnupg apt-transport-https curl wget kubeadm=1.25.5-00 kubelet=1.25.5-00 kubectl=1.25.5-00"

    # Create a directory for the local repository
    REPO_DIR="/root/packages"

    if [ -d "$REPO_DIR" ]; then
        rm -rf "$REPO_DIR"
    fi

    mkdir -p "$REPO_DIR"

    cd $REPO_DIR

    dpkg -l | grep "^ii" | awk ' {print $2} ' | xargs sudo apt-get -y --force-yes install --reinstall --download-only

    # Loop through the package list and download dependencies
    for package in $PACKAGE_LIST; do
        echo "Downloading : $package"
        apt-cache depends $package
        #apt-get download "$package" -o=dir::cache="$REPO_DIR" --print-uris | awk '{print $1}' | xargs -I {} wget -P "$REPO_DIR" {}

        for i in $(apt-cache depends $package | grep -E 'Depends|Recommends|Suggests' | cut -d ':' -f 2,3 | sed -e s/'<'/''/ -e s/'>'/''/); do sudo apt-get download $i 2>>errors.txt; done

        # apt-get download $i
        apt-get -y install --reinstall --download-only $i

    done

    cp /var/cache/apt/archives

    # Create a Packages file for the local repository
    cd "$REPO_DIR" || exit
    dpkg-scanpackages . /dev/null | gzip -9c >Packages.gz
    dpkg-scanpackages . /dev/null | gzip -9c >Release

    rm /etc/apt/sources.list.d/docker.list /etc/apt/sources.list.d/kubernetes.list

    mkdir $REPO_DIR/containerd
    cd $REPO_DIR/containerd
    curl -s -O https://github.com/containerd/containerd/releases/download/v1.6.12/containerd-1.6.12-linux-amd64.tar.gz

    cd $REPO_DIR
    cd ..

    tar czf /tmp/packages.tar.gz packages

}

setup_repo_ubuntu() {

    apt-get -qq -y update

    mkdir -p $BUNDLE_DIR $BUNDLE_DIR/packages $BUNDLE_DIR/configs

    cp /tmp/*.yaml $BUNDLE_DIR/configs

    if [ $OFFLINE_INSTALL = "false" ]; then
        echo "$(date) : Setting up online Repos"
        sudo install -m 0755 -d /etc/apt/keyrings
        if [ ! -f /etc/apt/keyrings/docker.gpg ]; then
            curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
            sudo chmod a+r /etc/apt/keyrings/docker.gpg
        fi

        # Step 3 : Use the following command to set up the repository:
        if [ ! -f /etc/apt/sources.list.d/docker.list ]; then
            echo \
                "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu focal stable" |
                sudo tee /etc/apt/sources.list.d/docker.list >/dev/null
        fi

        if [ ! -f /etc/apt/keyrings/kubernetes-archive-keyring.gpg ]; then
            curl -fsSLo /etc/apt/kubernetes-archive-keyring.gpg https://dl.k8s.io/apt/doc/apt-key.gpg
        fi

        # step 3: Add the Kubernetes apt repository:
        if [ ! -f /etc/apt/sources.list.d/kubernetes.list ]; then
            echo "deb [signed-by=/etc/apt/kubernetes-archive-keyring.gpg] https://apt.kubernetes.io/ kubernetes-xenial main" | tee /etc/apt/sources.list.d/kubernetes.list
        fi

        if [ -f /etc/apt/sources.list.d/localKuber.list ]; then
            rm /etc/apt/sources.list.d/localKuber.list
        fi

    else
        echo "$(date) : Setting up offline Repos"
        if [ -f /etc/apt/sources.list.d/localKuber.list ]; then
            rm /etc/apt/sources.list.d/localKuber.list
        fi

        if [ -f /etc/apt/sources.list.d/docker.list ]; then
            rm /etc/apt/sources.list.d/docker.list
        fi

        if [ -f /etc/apt/sources.list.d/docker.list ]; then
            rm /etc/apt/sources.list.d/kubernetes.list
        fi

        tar -C $BUNDLE_DIR -xf /tmp/packages.tar.gz
        echo "deb [trusted=yes] file:$BUNDLE_DIR/packages ./" >>/etc/apt/sources.list.d/localKuber.list
    fi
    apt-get -qq -y update

}

setup_kubernetes_ubuntu() {

    sudo apt-get -qq -y update
    sudo apt-get -qq -y install ca-certificates curl gnupg docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

    # Step 3: Verify that the Docker Engine installation is successful by running the hello-world image.
    sudo docker run hello-world

}

setup_kubernetes() {
    # Install Kubernetes
    apt-get -qq -y update
    apt-get install -qq -y apt-transport-https ca-certificates curl wget

    cat <<EOF | tee /etc/modules-load.d/k8s.conf
    overlay
    br_netfilter
EOF

    modprobe overlay
    modprobe br_netfilter

    # sysctl params required by setup, params persist across reboots
    cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
    net.bridge.bridge-nf-call-iptables  = 1
    net.bridge.bridge-nf-call-ip6tables = 1
    net.ipv4.ip_forward                 = 1
EOF

    # Apply sysctl params without reboot
    sudo sysctl --system

    # Step 4: Update apt package index, install kubelet, kubeadm and kubectl, and pin their version:
    apt-get -qq -y update
    # apt remove -y --purge kubelet kubeadm kubectl
    apt install -qq -y kubeadm=1.25.5-00 kubelet=1.25.5-00 kubectl=1.25.5-00
    # apt install -qq -y kubeadm kubelet kubectl
    apt-mark hold kubelet kubeadm kubectl

    # wget https://github.com/containerd/containerd/releases/download/v1.6.12/containerd-1.6.12-linux-amd64.tar.gz
    mkdir -p $BUNDLE_DIR/packages/containerd
    # tar -C $BUNDLE_DIR/packages/containerd -xf /tmp/containerd-1.6.12-linux-amd64.tar.gz
    systemctl stop containerd
    cp -r bin/ /usr/bin/
    systemctl start containerd

    rm /etc/containerd/config.toml
    systemctl restart containerd

    # with below calico, it has to be below range
    kubeadm init --pod-network-cidr=192.168.0.0/16

    if [ -d $HOME/.kube ]; then
        rm -rf $HOME/.kube
    fi

    mkdir -p $HOME/.kube
    cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
    chown $(id -u):$(id -g) $HOME/.kube/config

    kubectl create -f $BUNDLE_DIR/configs/tigera-operator.yaml

    kubectl create -f $BUNDLE_DIR/configs/custom-resources.yaml

    # watch kubectl get pods -n calico-system

    echo "watch kubectl get pods -n calico-system"

    # Wait for Calico
    sleep 60

    kubectl taint nodes --all node-role.kubernetes.io/control-plane-
    kubectl taint nodes --all node-role.kubernetes.io/master-

    kubectl get nodes

    echo "kubectl get nodes"

}

usage() {
    echo "$0 download|install"
}

opt="$1"

case "$opt" in
"download")
    download_ubuntu_packages
    ;;
"install")
    setup_repo_ubuntu
    # setup_docker_ubuntu
    setup_kubernetes_ubuntu
    ;;
*)
    usage
    exit 1
    ;;
esac
