#!/bin/bash

# Change below two variables for different clients.
NAMESPACE=bosler-staging
VALUES=stagingCluster.yaml

OFFLINE_INSTALL="false"

DOWNLOAD_IMAGES="true"
BUNDLE_DIR=/bosler/bundle
PROJECT_ID=octoctbos
ARTIFACTORY_PATH="europe-west1-docker.pkg.dev/$PROJECT_ID/bosler-cr"

export VERSION_boson=0.2.12
export VERSION_frontend=0.1.47
export VERSION_funnel=0.1.5
export VERSION_parler=0.0.13
export VERSION_shyne=0.0.95
export VERSION_callisto=0.0.2
export VERSION_julia=0.0.2
export VERSION_shs=0.0.2
export VERSION_bosler_docs=0.0.3

export LANG=en_US.UTF-8
export LANGUAGE=en_US.UTF-8
export LC_COLLATE=C
export LC_CTYPE=en_US.UTF-8

# localedef -i en_US -f UTF-8 en_US.UTF-8

# setting MYOS variable
BUNDLE_OS="Ubuntu"
# BUNDLE_OS="CentOS"

bundle() {

    read -p "Do you want to create fresh bundle (it downloads again)? (Y/N): " prune

    if [[ $prune == [yY] || $prune == [yY][eE][sS] ]]; then
        if [ -d bundle ]; then
            rm -rf bundle
        fi
        if [ -f bosler_bundle.tar.gz ]; then
            rm bosler_bundle.tar.gz
        fi

        docker system prune -a -f
    fi

    if [ $BUNDLE_OS = "CentOS" ]; then
        if [ ! -f OS/CentOS/packages.tar.gz ]; then
            echo "ERROR : packages.tar.gz needed to build the bundle, this file has all rpms for docker and kubernetes"
        fi

        if [ ! -d bundle/packages ]; then
            mkdir -p bundle/packages
        fi

        if [ ! -f bundle/packages/packages.tar.gz ]; then
            cp OS/CentOS/packages.tar.gz bundle/packages
        fi

    fi

    if [ $BUNDLE_OS = "Ubuntu" ]; then
        if [ ! -f OS/Ubuntu/packages.tar.gz ]; then
            echo "ERROR : packages.tar.gz needed to build the bundle, this file has all rpms for docker and kubernetes"
        fi

        if [ ! -d bundle/packages ]; then
            mkdir -p bundle/packages
        fi

        if [ ! -f bundle/packages/packages.tar.gz ]; then
            cp OS/Ubuntu/packages.tar.gz bundle/packages
        fi
    fi

    if [ ! -d bundle/configs ]; then
        mkdir -p bundle/configs
    fi

    if [ $DOWNLOAD_IMAGES = "true" ]; then
        # save images
        # sudo docker save $(sudo docker images --format '{{.Repository}}:{{.Tag}}') -o kubernetes-docker-images.tar
        if [ ! -d bundle/images ]; then
            mkdir -p bundle/images
            mkdir -p bundle/images/kubernetes
            mkdir -p bundle/images/bosler
        fi

        # to get tags : sudo docker images --format '{{.Repository}}:{{.Tag}}'
        declare -a images=(
            "$ARTIFACTORY_PATH/frontend:$VERSION_frontend"
            "$ARTIFACTORY_PATH/boson:$VERSION_boson"
            "$ARTIFACTORY_PATH/julia:$VERSION_julia"
            "$ARTIFACTORY_PATH/funnel:$VERSION_funnel"
            "$ARTIFACTORY_PATH/parler:$VERSION_parler"
            "$ARTIFACTORY_PATH/callisto:$VERSION_callisto"
            "$ARTIFACTORY_PATH/shyne:$VERSION_shyne"
            "$ARTIFACTORY_PATH/bosler-docs:$VERSION_bosler_docs"
            "$ARTIFACTORY_PATH/spark-history-server:$VERSION_shs"

            "minio/minio:latest"
            "docker.io/calico/apiserver:v3.25.1"
            "docker.io/calico/cni:v3.25.1"
            "docker.io/calico/csi:v3.25.1"
            "docker.io/calico/kube-controllers:v3.25.1"
            "docker.io/calico/node-driver-registrar:v3.25.1"
            "docker.io/calico/node:v3.25.1"
            "docker.io/calico/pod2daemon-flexvol:v3.25.1"
            "docker.io/calico/typha:v3.25.1"
            "docker.io/library/postgres:11-alpine"
            "docker.io/library/redis:latest"
            "ghcr.io/googlecloudplatform/spark-operator:v1beta2-1.3.8-3.1.1"
            "k8s.gcr.io/ingress-nginx/controller:v1.1.1"
            "k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.1.1"
            "quay.io/metallb/controller:v0.11.0"
            "quay.io/metallb/speaker:v0.11.0"
            "quay.io/tigera/operator:v1.29.3"
            "registry.k8s.io/coredns/coredns:v1.9.3"
            "registry.k8s.io/etcd:3.5.6-0"
            "registry.k8s.io/kube-apiserver:v1.25.5"
            "registry.k8s.io/kube-controller-manager:v1.25.5"
            "registry.k8s.io/kube-proxy:v1.25.5"
            "registry.k8s.io/kube-scheduler:v1.25.5"
            "registry.k8s.io/kube-apiserver:v1.25.10"
            "registry.k8s.io/kube-controller-manager:v1.25.10"
            "registry.k8s.io/kube-proxy:v1.25.10"
            "registry.k8s.io/kube-scheduler:v1.25.10"
            "registry.k8s.io/pause:3.6"
            "registry.k8s.io/pause:3.8"

        )

        for image_tag in "${images[@]}"; do
            image_name=$(echo $image_tag | awk -F/ '{print $NF}' | tr ":" "-")

            # if [ ! -f bundle/images/bosler/$image_name.tar ] || [ ! -f bundle/images/kubernetes/$image_name.tar ]; then
            if [ ! -f "bundle/images/bosler/$image_name.tar" ] && [ ! -f "bundle/images/kubernetes/$image_name.tar" ]; then
                echo "Downloading image : $image_tag"
                docker pull --platform linux/amd64 $image_tag >/dev/null

                if [[ $image_tag == *"$ARTIFACTORY_PATH"* ]]; then
                    # Replace the variable with the new value

                    new_tag=$(echo $image_tag | sed 's,'"$ARTIFACTORY_PATH"',bosler.io,g')

                    docker tag $image_tag $new_tag
                    docker save --output bundle/images/bosler/$image_name.tar $new_tag >/dev/null
                else
                    docker save --output bundle/images/kubernetes/$image_name.tar $image_tag >/dev/null
                fi

            fi
        done

        if [ $BUNDLE_OS = "CentOS" ]; then
            if [ -f kubernetes-docker-images.tar ]; then
                cp OS/CentOS/kubernetes-docker-images.tar bundle/images/kubernetes
            else
                echo "ERROR: kubernetes-docker-images.tar is needed in this folder to create bundle"
                exit 2
            fi
        fi
    fi

    # Helm & Containerd
    if [ ! -f bundle/packages/helm-v3.8.0-rc.1-linux-amd64.tar.gz ]; then
        if [ ! -d bundle/packages ]; then
            mkdir -p bundle/packages
        fi

        curl -sSL -o bundle/packages/helm-v3.8.0-rc.1-linux-amd64.tar.gz https://get.helm.sh/helm-v3.8.0-rc.1-linux-amd64.tar.gz
        curl -sSL -o bundle/packages/containerd-1.6.12-linux-amd64.tar.gz https://github.com/containerd/containerd/releases/download/v1.6.12/containerd-1.6.12-linux-amd64.tar.gz
    fi

    # scripts
    cp build_bosler_offline.sh bundle/

    if [ $BUNDLE_OS = "CentOS" ]; then
        cp OS/CentOS/bosler.repo bundle/configs
        cp OS/CentOS/kube-flannel.yml bundle/configs
    fi

    if [ $BUNDLE_OS = "Ubuntu" ]; then
        cp OS/Ubuntu/tigera-operator.yaml bundle/configs
        cp OS/Ubuntu/custom-resources.yaml bundle/configs
    fi

    # Deployments
    if [ ! -d bundle/deployments ]; then
        mkdir -p bundle/deployments
    fi

    cp -r onPremiseInstall/configurations bundle/deployments
    cp -r onPremiseInstall/spark bundle/deployments
    cp -r onPremiseInstall/bosler_spark_env.sh bundle/deployments
    cp env.sh bundle/

    # create tar bundle
    if [ ! -f bosler_bundle.tar.gz ]; then
        tar cfz bosler_bundle.tar.gz bundle
    fi

    # spark operator
    # helm repo add spark-operator https://googlecloudplatform.github.io/spark-on-k8s-operator
    # helm pull spark-operator

    echo ""
    echo "**** : Bundle created successfully : ****"
    echo ""
    echo "Note : Copy to the installation server : (e.g. gcloud compute scp bosler_bundle.tar.gz bosler-instance-with-access:~/ ) "
    echo ""
    echo "Login to server : gcloud beta compute ssh --zone "$COMPUTE_ZONE" "bosler-instance-with-access"  --tunnel-through-iap --project "$PROJECT_ID""
    echo ""
    echo "Then Run :  sudo mkdir /bosler && sudo tar -C /bosler -xf bosler_bundle.tar.gz && sudo /bosler/bundle/build_bosler_offline.sh prepare"
    echo ""

}

setup_repo_centos() {

    sudo cd /bosler/bundle/packages

    sudo cd $BUNDLE_DIR/packages
    sudo tar -C $BUNDLE_DIR -xf $BUNDLE_DIR/packages/packages.tar.gz

    # install create repo
    sudo yum -y -q --disablerepo=* localinstall /bosler/bundle/packages/deltarpm-3.6-3.el7.x86_64.rpm
    sudo yum -y -q --disablerepo=* localinstall /bosler/bundle/packages/python-deltarpm-3.6-3.el7.x86_64.rpm
    sudo yum -y -q --disablerepo=* localinstall /bosler/bundle/packages/libxml2-python-2.9.1-6.el7_9.6.x86_64.rpm
    sudo yum -y -q --disablerepo=* localinstall /bosler/bundle/packages/createrepo-0.9.9-28.el7.noarch.rpm

    sudo createrepo /bosler/bundle/packages >/dev/null

    sudo cp $BUNDLE_DIR/configs/bosler.repo /etc/yum.repos.d

}

setup_repo_ubuntu() {

    apt-get -qq -y update

    mkdir -p $BUNDLE_DIR $BUNDLE_DIR/packages $BUNDLE_DIR/configs

    if [ $OFFLINE_INSTALL = "false" ]; then
        echo "$(date) : Setting up online Repos"
        sudo install -m 0755 -d /etc/apt/keyrings
        if [ ! -f /etc/apt/keyrings/docker.gpg ]; then
            curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
            sudo chmod a+r /etc/apt/keyrings/docker.gpg
        fi

        # Step 3 : Use the following command to set up the repository:
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

        if [ -f /etc/apt/sources.list.d/kubernetes.list ]; then
            rm /etc/apt/sources.list.d/kubernetes.list
        fi

        tar -C $BUNDLE_DIR -xf $BUNDLE_DIR/packages/packages.tar.gz
        echo "deb [trusted=yes] file:$BUNDLE_DIR/packages ./" >>/etc/apt/sources.list.d/localKuber.list
    fi
    apt-get -qq -y update

}

setup_kubernetes_ubuntu() {
    # Install Kubernetes
    apt-get -qq -y update
    apt-get install -qq -y apt-transport-https ca-certificates curl wget vim

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
    sudo sysctl --system >/dev/null

    # Step 4: Update apt package index, install kubelet, kubeadm and kubectl, and pin their version:
    apt-get -qq -y update
    # apt remove -y --purge kubelet kubeadm kubectl
    apt install -qq -y kubeadm=1.25.5-00 kubelet=1.25.5-00 kubectl=1.25.5-00 containerd
    # apt install -qq -y kubeadm kubelet kubectl
    apt-mark hold kubelet kubeadm kubectl

    # wget https://github.com/containerd/containerd/releases/download/v1.6.12/containerd-1.6.12-linux-amd64.tar.gz
    mkdir -p $BUNDLE_DIR/packages/containerd
    tar -C $BUNDLE_DIR/packages/containerd -xf $BUNDLE_DIR/packages/containerd-1.6.12-linux-amd64.tar.gz
    systemctl stop containerd
    cp -r $BUNDLE_DIR/packages/containerd/bin/* /usr/bin/

    if [ ! -d /etc/containerd/ ]; then
        mkdir -p /etc/containerd/
    fi

    containerd config default >/etc/containerd/config.toml

    sed -i 's/SystemdCgroup \= false/SystemdCgroup \= true/g' /etc/containerd/config.toml

    systemctl daemon-reload

    systemctl start containerd
    systemctl enable containerd

    # if [ -f /etc/containerd/config.toml ]; then
    #     rm /etc/containerd/config.toml
    #     systemctl restart containerd
    # fi

}

setup_container() {

    ##### CentOS 7 config
    if [ $MYOS = "CentOS" ]; then
        echo "$(date) : Setting up CentOS 7 with Docker "

        if [ $OFFLINE_INSTALL = "false" ]; then
            yum install -y -q vim yum-utils device-mapper-persistent-data lvm2
        else
            setup_repo_centos
        fi

        systemctl disable --now firewalld
    fi

    ##### Ubuntu config
    if [ $MYOS = "Ubuntu" ]; then
        setup_repo_ubuntu
    fi

}

load_images() {
    if ! [ $USER = root ]; then
        echo run this script with sudo
        exit 3
    fi
    # load images
    # Delete all images
    # ctr -n k8s.io images rm $(ctr -n k8s.io images ls -q)
    find $BUNDLE_DIR/images/ -name \*tar | grep -v "._" | while read line; do

        # Images have to be loaded into k8s.io namespace otherwise kubernetes deployments will try to pull them
        ctr -n k8s.io images import $line
    done

    # helm
    sudo tar -C /usr/bin -xf $BUNDLE_DIR/packages/helm-v3.8.0-rc.1-linux-amd64.tar.gz --strip-components=1
}

setup_kube_tools() {

    ##### CentOS 7 config
    if [ $MYOS = "CentOS" ]; then

        if [ $OFFLINE_INSTALL = "false" ]; then
            cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://packages.cloud.google.com/yum/repos/kubernetes-el7-\$basearch
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
exclude=kubelet kubeadm kubectl
EOF
        fi

        # Set SELinux in permissive mode (effectively disabling it)
        setenforce 0
        sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config

        # disable swap (assuming that the name is /dev/centos/swap
        sed -i 's/^\/dev\/mapper\/centos-swap/#\/dev\/mapper\/centos-swap/' /etc/fstab
        sudo sed -i 's/^\/dev\/mapper\/centos-swap/#\/dev\/mapper\/centos-swap/' /etc/fstab
        sudo sed -i '/swap/d' /etc/fstab
        sudo swapoff -a
        swapoff -a

        if [ $OFFLINE_INSTALL = "true" ]; then
            if [ -f /etc/yum.repos.d/bosler.repo ]; then
                yum install -y -q --disablerepo=* --enablerepo=bosler kubelet kubeadm kubectl --disableexcludes=kubernetes
            else
                echo "ERROR : bosler repo file not found in /etc/yum/repos.d"
            fi
        else
            if [ -f /etc/yum.repos.d/bosler.repo ]; then
                yum install -y -q --disablerepo=bosler kubelet kubeadm kubectl --disableexcludes=kubernetes
            else
                yum install -y -q kubelet kubeadm kubectl --disableexcludes=kubernetes
            fi
        fi

        systemctl enable --now kubelet

        # Set iptables bridging
        cat <<EOF >/etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ip_forward                = 1
EOF
        sysctl --systemun >/dev/null

    fi

    if [ $MYOS = "Ubuntu" ]; then
        # echo "Ubuntu will come soon"
        # exit 0

        setup_kubernetes_ubuntu
    fi

}

uninstall() {

    if [ $MYOS = "Ubuntu" ]; then
        kubeadm reset
        # on debian base
        sudo apt-get purge kubeadm kubectl kubelet kubernetes-cni kube* containerd
        # on debian base
        sudo apt-get autoremove
    fi

    if [ $MYOS = "CentOS" ]; then
        #on centos base
        sudo yum remove kubeadm kubectl kubelet kubernetes-cni kube*

        #on centos base
        sudo yum autoremove
    fi

    rm -rf ~/.kube
    rm -rf /etc/cni /etc/kubernetes /var/lib/dockershim /var/lib/etcd /var/lib/kubelet /var/lib/etcd2/ /var/run/kubernetes ~/.kube/*
    rm -rf /var/lib/docker /etc/docker /var/run/docker.sock
    rm -f /etc/apparmor.d/docker /etc/systemd/system/etcd*

    # ctr -n k8s.io images rm $(ctr -n k8s.io images ls -q)

}

install() {
    echo -n "Do you wish to create kubernetes cluster with values $VALUES ? (Y|N) : "
    read confirm
    if [[ "$confirm" == [yY] || "$confirm" == [yY][eE][sS] ]]; then
        # if [ ! -f /var/tmp/bosler_install_prepared ]; then
        #     echo "Error: First step of installation was not done."
        #     usage
        #     exit 3
        # fi

        ##### CentOS 7 config
        if [ $MYOS = "CentOS" ]; then

            # kubernetes now --ignore-preflight-errors=All
            kubeadm init --pod-network-cidr=10.244.0.0/16

            if [ -d $HOME/.kube ]; then
                rm -rf $HOME/.kube
            fi
            mkdir -p $HOME/.kube
            sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
            sudo chown $(id -u):$(id -g) $HOME/.kube/config

            # as normal user
            kubectl apply -f $BUNDLE_DIR/configs/kube-flannel.yml

            # below is only for single node cluster, if multi node then we have to join the cluster
            kubectl taint nodes --all node-role.kubernetes.io/master-
        fi

        if [ $MYOS = "Ubuntu" ]; then
            kubeadm init --pod-network-cidr=10.244.0.0/16
            # 192.168.0.0/16

            if [ -d $HOME/.kube ]; then
                rm -rf $HOME/.kube
            fi

            mkdir -p $HOME/.kube
            cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
            chown $(id -u):$(id -g) $HOME/.kube/config

            echo "Waiting for 60 secs"
            sleep 60
            # kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.25.1/manifests/tigera-operator.yaml
            # kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.25.1/manifests/custom-resources.yaml

            # Reference : https://docs.tigera.io/calico/latest/getting-started/kubernetes/quickstart
            # export BUNDLE_DIR=/bosler/bundle
            kubectl create -f $BUNDLE_DIR/configs/tigera-operator.yaml
            sleep 10
            kubectl create -f $BUNDLE_DIR/configs/custom-resources.yaml # it is very important to use this file from local and not from internet and match IP Network as above from kubeadm init command

            # Wait for Calico
            echo "Waiting for 60 secs"
            sleep 60

            kubectl taint nodes --all node-role.kubernetes.io/control-plane-
            kubectl taint nodes --all node-role.kubernetes.io/master-

            kubectl get nodes
        fi

        # clean up
        [ -f /var/tmp/bosler_install_prepared ] && rm /var/tmp/bosler_install_prepared
    fi
}

setup_bosler() {

    # Now we configure kubernetes
    # this is special for onPremise
    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/metallb/namespace.yaml
    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/metallb/metallb.yaml
    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/metallb/metallb-configmap.yaml # need to run manually

    sudo kubectl create -f $BUNDLE_DIR/deployments/configurations/metallb/ingress-nginx-controller-deploy.yaml
    sudo kubectl -n ingress-nginx annotate ingressclasses nginx ingressclass.kubernetes.io/is-default-class="true" --overwrite

    cd $BUNDLE_DIR/deployments/configurations/helm

    echo -n "Do you wish to run helm after images are ready? (Y|N) : "
    read confirm

    if [[ "$confirm" == [yY] || "$confirm" == [yY][eE][sS] ]]; then
        helm install bosler charts/bosler-gke -f charts/bosler-gke/$VALUES

        # helm upgrade bosler charts/bosler-gke -f charts/$HELM_CHART/$HELM_VALUES
    fi

    # echo -n "Do you wish to run HDFS helm? (Y|N) : "
    # read confirm

    # if [[ "$confirm" == [yY] || "$confirm" == [yY][eE][sS] ]]; then
    #     helm install bosler-hdfs charts/hdfs-charts
    # fi

    # echo -n "Do you wish to run NFS Server helm? (Y|N) : "
    # read confirm

    # if [[ "$confirm" == [yY] || "$confirm" == [yY][eE][sS] ]]; then
    #     helm install bosler-nfs charts/nfs-server
    # fi

    cd - >/dev/null

    # setup spark
    helm install -f $BUNDLE_DIR/deployments/spark/spark-helm-values.yaml bosler $BUNDLE_DIR/deployments/spark/spark-operator-1.1.27.tgz --namespace $NAMESPACE --set webhook.enable=true
    kubectl -n $NAMESPACE create serviceaccount spark
    kubectl create clusterrolebinding spark-role --clusterrole=cluster-admin --user=system:serviceaccount:$NAMESPACE:spark
    # below is run build from cluster
    kubectl create clusterrolebinding spark-role-default --clusterrole=cluster-admin --user=system:serviceaccount:$NAMESPACE:default

    # Go in sleep loop until db started
    echo -n "waiting for database to start"
    while true; do
        sudo kubectl -n $NAMESPACE get pods | grep boson-db | grep Running >/dev/null
        if [ $? == 0 ]; then
            echo "|"
            sleep 5
            # kubectl exec -it boson-db-5dd84575cf-z6l2j -- bin/bash -c "su - postgres -c \"psql -c 'DROP DATABASE boson;'\""
            # NAMESPACE=bosler-demo
            sudo kubectl -n $NAMESPACE exec -it $(kubectl -n $NAMESPACE get pods | awk '{print $1}' | grep boson-db) -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE boson;'\""
            sudo kubectl -n $NAMESPACE exec -it $(kubectl -n $NAMESPACE get pods | awk '{print $1}' | grep boson-db) -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE kepler;'\""
            break
        fi
        echo -n "."
        sleep 5
    done

    #create dataset bucket in minio
    # sudo kubectl -n $NAMESPACE exec -it $(kubectl -n $NAMESPACE get pods | awk '{print $1}' | grep minio) -- /bin/bash -c "mkdir -p /storage/datasets"
    # sudo kubectl -n $NAMESPACE exec -it $(kubectl -n $NAMESPACE get pods | awk '{print $1}' | grep minio) -- /bin/bash -c "mkdir -p /storage/spark-streaming/checkpoint"

    # Go in sleep loop until boson started
    echo -n "waiting for backend/boson to start"
    while true; do
        sudo kubectl -n $NAMESPACE get pods | grep -v boson-db | grep boson | grep Running >/dev/null
        if [ $? == 0 ]; then
            echo "|"
            break
        fi
        echo -n "."
        sleep 5
    done

    # safety net below
    # NAMESPACE=bosler-demo
    kubectl -n $NAMESPACE exec -it $(kubectl -n $NAMESPACE get pods | awk '{print $1}' | grep boson-db) -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE boson;'\""
    kubectl -n $NAMESPACE exec -it $(kubectl -n $NAMESPACE get pods | awk '{print $1}' | grep boson-db) -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE kepler;'\""
    # sudo kubectl -n $NAMESPACE exec -it $(kubectl -n $NAMESPACE get pods | awk '{print $1}' | grep minio) -- /bin/bash -c "mkdir -p /storage/datasets"
    # sudo kubectl -n $NAMESPACE exec -it $(kubectl -n $NAMESPACE get pods | awk '{print $1}' | grep minio) -- /bin/bash -c "mkdir -p /storage/spark-streaming/checkpoint"

    echo -n "Re-Run the helm  upgrade $VALUES ? (Y|N) : "
    read confirm

    if [[ "$confirm" == [yY] || "$confirm" == [yY][eE][sS] ]]; then
        cd $BUNDLE_DIR/deployments/configurations/helm
        helm upgrade bosler charts/bosler-gke -f charts/bosler-gke/$VALUES

        cd -
    fi

}

prepare() {

    if ! [ $USER = root ]; then
        echo run this script with sudo
        exit 3
    fi

    BUNDLE_DIR=/bosler/bundle
    cd $BUNDLE_DIR/packages

    # setting MYOS variable
    MYOS=$(hostnamectl | awk '/Operating/ { print $3 }')
    OSVERSION=$(hostnamectl | awk '/Operating/ { print $4 }')

    setup_container
    setup_kube_tools

    # if [ $OFFLINE_INSTALL = "true" ]; then
    load_images
    # echo " not loading images for test"
    # fi

    sudo echo ""
    sudo echo "**** : The preparation of setup has been completed successfully : ****"
    sudo echo ""
    sudo echo " !!! You could modify $BUNDLE_DIR/deployments/configurations/metallb/metallb-configmap.yaml  before reboot to add metallb IPs !!! "
    sudo echo ""
    sudo echo "Note : This setup requires a reboot at this stage (Reboot not required for Ubuntu). After reboot sudo $0 install"
    sudo echo ""

    sudo echo "Step1" >/var/tmp/bosler_install_prepared
}

usage() {
    echo "$0 bundle|prepare|ubuntuInstall|CentOSprepare|CentOSinstall|setup_bosler"
}

opt="$1"

case "$opt" in
"bundle")
    bundle
    ;;
"ubuntuInstall")

    if ! [ $USER = root ]; then
        echo run this script with sudo
        exit 3
    fi
    # untar first in server
    # sudo mkdir /bosler && sudo tar -C /bosler -xf bosler_bundle.tar.gz && sudo /bosler/bundle/build_bosler_offline.sh prepare
    prepare

    BUNDLE_DIR=/bosler/bundle
    cd $BUNDLE_DIR/packages

    # setting MYOS variable
    MYOS=$(hostnamectl | awk '/Operating/ { print $3 }')
    OSVERSION=$(hostnamectl | awk '/Operating/ { print $4 }')

    install
    setup_bosler
    sudo echo ""
    echo "**** : Setup completed successfully : ****"
    sudo echo ""
    sudo echo "!! Note !!: make sure to modify $BUNDLE_DIR/deployments/configurations/metallb/metallb-configmap.yaml and apply it, otherwise ingress will not work."
    sudo echo ""

    ;;
"CentOSprepare")
    # untar first in server
    # sudo mkdir /bosler && sudo tar -C /bosler -xf bosler_bundle.tar.gz && sudo /bosler/bundle/build_bosler_offline.sh prepare
    prepare
    ;;
"CentOSinstall")

    if ! [ $USER = root ]; then
        echo run this script with sudo
        exit 3
    fi

    BUNDLE_DIR=/bosler/bundle
    cd $BUNDLE_DIR/packages

    # setting MYOS variable
    MYOS=$(hostnamectl | awk '/Operating/ { print $3 }')
    OSVERSION=$(hostnamectl | awk '/Operating/ { print $4 }')

    install
    setup_bosler
    sudo echo ""
    echo "**** : Setup completed successfully : ****"
    sudo echo ""
    sudo echo "!! Note !!: make sure to modify $BUNDLE_DIR/deployments/configurations/metallb/metallb-configmap.yaml and apply it, otherwise ingress will not work."
    sudo echo ""
    ;;
"setup_bosler")
    setup_bosler
    ;;
*)
    usage
    exit 1
    ;;
esac
