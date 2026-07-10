#!/bin/bash


OFFLINE_INSTALL="true"
BUNDLE_DIR=/movetodata/bundle
PROJECT_ID=movetodata-foo
ARTIFACTORY_PATH="europe-west1-docker.pkg.dev/$PROJECT_ID/movetodata-cr"

export LANG=en_US.UTF-8
export LANGUAGE=en_US.UTF-8
export LC_COLLATE=C
export LC_CTYPE=en_US.UTF-8

bundle() {

    read -p "Do you want to create fresh bundle (it downloads again)? (Y/N): " prune
    
    if [[ $prune == [yY] || $prune == [yY][eE][sS] ]]; then
        if [ -d bundle ]; then
            rm -rf bundle 
        fi
        if [ -f movetodata_bundle.tar.gz ]; then
            rm movetodata_bundle.tar.gz
        fi

        docker system prune -a -f 
    fi

    if [ ! -f packages.tar.gz ]; then
        echo "ERROR : packages.tar.gz needed to build the bundle, this file has all rpms for docker and kubernetes"
    fi

    if [ ! -d bundle/packages ]; then
        mkdir -p bundle/packages
    fi

    if [ ! -d bundle/configs ]; then
        mkdir -p bundle/configs
    fi

    if [ ! -f bundle/packages/packages.tar.gz ]; then
        cp packages.tar.gz bundle/packages
    fi

    if [ $OFFLINE_INSTALL = "true" ]; then
        # save images
        # sudo docker save $(sudo docker images --format '{{.Repository}}:{{.Tag}}') -o kubernetes-docker-images.tar
        if [ ! -d bundle/images ]; then
            mkdir -p bundle/images
        fi

        # to get tags : sudo docker images --format '{{.Repository}}:{{.Tag}}'
        declare -a images=(
                        "$ARTIFACTORY_PATH/frontend"
                        "$ARTIFACTORY_PATH/boson"
                        "$ARTIFACTORY_PATH/julia"
                        "$ARTIFACTORY_PATH/funnel"
                        "$ARTIFACTORY_PATH/parler"
                        "$ARTIFACTORY_PATH/spark-history-server"

                        "minio/minio:latest"
                        "postgres:11-alpine"
                        "k8s.gcr.io/ingress-nginx/controller:v1.1.1"
                        "k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.1.1"
                        "quay.io/metallb/speaker:v0.11.0"
                        "quay.io/metallb/controller:v0.11.0"
                        "gcr.io/spark-operator/spark-operator"
                        )

        
        for image_tag in "${images[@]}"
        do
            image_name=$(echo $image_tag|awk -F/ '{print $NF}')

            if [ ! -f bundle/images/$image_name.tar ]; then
                echo "Downloading image : $image_tag"
                docker pull --platform linux/amd64 $image_tag > /dev/null
                docker save --output bundle/images/$image_name.tar $image_tag > /dev/null
            fi
        done

        if [ -f kubernetes-docker-images.tar ]; then
            cp kubernetes-docker-images.tar bundle/images
        else
            echo "ERROR: kubernetes-docker-images.tar is needed in this folder to create bundle"
            exit 2
        fi
    fi


    # Helm
    if [ ! -f bundle/packages/helm-v3.8.0-rc.1-linux-amd64.tar.gz ]; then
        curl -sSL -o bundle/packages/helm-v3.8.0-rc.1-linux-amd64.tar.gz https://get.helm.sh/helm-v3.8.0-rc.1-linux-amd64.tar.gz
    fi

    # scripts
    cp build_movetodata_offline.sh bundle/
    cp movetodata.repo bundle/configs
    cp kube-flannel.yml bundle/configs

    # Deployments
    if [ ! -d bundle/deployments ]; then
        mkdir -p bundle/deployments
    fi

    cp -r onPremiseInstall/configurations bundle/deployments
    cp -r onPremiseInstall/spark bundle/deployments
    cp -r onPremiseInstall/movetodata_spark_env.sh bundle/deployments

    # create tar bundle
    if [ ! -f movetodata_bundle.tar.gz ]; then
        tar cfz movetodata_bundle.tar.gz bundle
    fi

    # spark operator
    # helm repo add spark-operator https://googlecloudplatform.github.io/spark-on-k8s-operator
    # helm pull spark-operator

    echo ""
    echo "**** : Bundle created successfully : ****"
    echo ""
    echo "Note : Copy to the installation server and run :  sudo mkdir /movetodata && sudo tar -C /movetodata -xf movetodata_bundle.tar.gz && sudo /movetodata/bundle/build_movetodata_offline.sh prepare"
    echo ""

}

setup_local_repo()
{

    sudo cd /movetodata/bundle/packages

    sudo cd $BUNDLE_DIR/packages
    sudo tar -C $BUNDLE_DIR -xf $BUNDLE_DIR/packages/packages.tar.gz

    # install create repo
    sudo yum -y -q --disablerepo=* localinstall /movetodata/bundle/packages/deltarpm-3.6-3.el7.x86_64.rpm
    sudo yum -y -q --disablerepo=* localinstall /movetodata/bundle/packages/python-deltarpm-3.6-3.el7.x86_64.rpm
    sudo yum -y -q --disablerepo=* localinstall /movetodata/bundle/packages/libxml2-python-2.9.1-6.el7_9.6.x86_64.rpm
    sudo yum -y -q --disablerepo=* localinstall /movetodata/bundle/packages/createrepo-0.9.9-28.el7.noarch.rpm

    sudo createrepo /movetodata/bundle/packages > /dev/null

    sudo cp $BUNDLE_DIR/configs/movetodata.repo /etc/yum.repos.d

}


load_images()
{
    if ! [ $USER = root ]
    then
            echo run this script with sudo
            exit 3
    fi
    # load images
    ls $BUNDLE_DIR/images/*tar* | while read line; do
        sudo docker load -q --input $line
    done

    # helm
    sudo tar -C /usr/bin -xf $BUNDLE_DIR/packages/helm-v3.8.0-rc.1-linux-amd64.tar.gz --strip-components=1
}

setup_container()
{


    if ! [ $USER = root ]
    then
            echo run this script with sudo
            exit 3
    fi

    # setting MYOS variable
    MYOS=$(hostnamectl | awk '/Operating/ { print $3 }')
    OSVERSION=$(hostnamectl | awk '/Operating/ { print $4 }')

    ##### CentOS 7 config
    if [ $MYOS = "CentOS" ]
    then
            echo "`date` : Setting up CentOS 7 with Docker "
            
            if [ $OFFLINE_INSTALL = "false" ]; then
                yum install -y -q vim yum-utils device-mapper-persistent-data lvm2
                yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
            else
                setup_local_repo
            fi

            # notice that only verified versions of Docker may be installed
            # verify the documentation to check if a more recent version is available

            if [ $OFFLINE_INSTALL = "true" ]; then
                if [ -f /etc/yum.repos.d/movetodata.repo ]; then
                    yum install -y -q --disablerepo=* --enablerepo=movetodata install docker-ce
                else
                    echo "ERROR : movetodata repo file not found in /etc/yum/repos.d"
                fi
            else
                if [ -f /etc/yum.repos.d/movetodata.repo ]; then
                    yum install -y -q --disablerepo=movetodata docker-ce
                else
                    yum install -y -q docker-ce
                fi
            fi
            
            [ ! -d /etc/docker ] && mkdir /etc/docker

            mkdir -p /etc/systemd/system/docker.service.d


            cat > /etc/docker/daemon.json <<- EOF
{
    "exec-opts": ["native.cgroupdriver=systemd"],
    "log-driver": "json-file",
    "log-opts": {
    "max-size": "100m"
    },
    "storage-driver": "overlay2",
    "storage-opts": [
    "overlay2.override_kernel_check=true"
    ]
}
EOF


            systemctl daemon-reload
            systemctl restart docker
            systemctl enable docker

            systemctl disable --now firewalld
    fi

    if [ $MYOS = "Ubuntu" ]
    then

            echo "Ubuntu will come soon"
            exit 0
            
            ### setting up container runtime prereq
            cat <<- EOF | sudo tee /etc/modules-load.d/containerd.conf
overlay
br_netfilter
EOF

            sudo modprobe overlay
            sudo modprobe br_netfilter

            # Setup required sysctl params, these persist across reboots.
            cat <<- EOF | sudo tee /etc/sysctl.d/99-kubernetes-cri.conf
net.bridge.bridge-nf-call-iptables  = 1
net.ipv4.ip_forward                 = 1
net.bridge.bridge-nf-call-ip6tables = 1
EOF

            
            # Apply sysctl params without reboot
            sudo sysctl --system > /dev/null 

            # (Install containerd)
            sudo apt-get update && sudo apt-get install -y containerd
            # Configure containerd
            sudo mkdir -p /etc/containerd
            containerd config default | sudo tee /etc/containerd/config.toml
            # Restart containerd
            sudo systemctl restart containerd       
    fi

}

setup_kube_tools()
{
    if ! [ $USER = root ]
    then
            echo run this script with sudo
            exit 3
    fi

    # setting MYOS variable
    MYOS=$(hostnamectl | awk '/Operating/ { print $3 }')
    OSVERSION=$(hostnamectl | awk '/Operating/ { print $4 }')

    ##### CentOS 7 config
    if [ $MYOS = "CentOS" ]
    then

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
                if [ -f /etc/yum.repos.d/movetodata.repo ]; then                    
                    yum install -y -q --disablerepo=* --enablerepo=movetodata kubelet kubeadm kubectl --disableexcludes=kubernetes
                else
                    echo "ERROR : movetodata repo file not found in /etc/yum/repos.d"
                fi
            else
                if [ -f /etc/yum.repos.d/movetodata.repo ]; then
                    yum install -y -q --disablerepo=movetodata kubelet kubeadm kubectl --disableexcludes=kubernetes
                else
                    yum install -y -q kubelet kubeadm kubectl --disableexcludes=kubernetes
                fi
            fi

            systemctl enable --now kubelet
    fi

    if [ $MYOS = "Ubuntu" ]
    then
            echo "Ubuntu will come soon"
            exit 0

            echo RUNNING UBUNTU CONFIG
            cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
        br_netfilter
EOF
            
            sudo apt-get update && sudo apt-get install -y apt-transport-https curl
            curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
            cat <<EOF | sudo tee /etc/apt/sources.list.d/kubernetes.list
        deb https://apt.kubernetes.io/ kubernetes-xenial main
EOF

            sudo apt-get update
            sudo apt-get install -y kubelet kubeadm kubectl
            sudo apt-mark hold kubelet kubeadm kubectl
            swapoff /swapfile
            
            sed -i 's/swapfile/#swapfile/' /etc/fstab
    fi

    # Set iptables bridging
    cat <<EOF >  /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
    sysctl --system > /dev/null
}

install()
{
    # if [ ! -f /tmp/movetodata_install_prepared ]; then
    #     echo "Error: First step of installation was not done."
    #     usage
    #     exit 3
    # fi

    if ! [ $USER = root ]
    then
            echo run this script with sudo
            exit 3
    fi

    BUNDLE_DIR=/movetodata/bundle
    sudo cd $BUNDLE_DIR/packages
    
    # kubernetes now --ignore-preflight-errors=All
    sudo kubeadm init --pod-network-cidr=10.244.0.0/16

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


    # install istio
    # curl -L https://istio.io/downloadIstio | sh -
    # sudo echo 'export PATH="$PATH:/home/centos/istio-1.12.2/bin"' >> /etc/profile
    # source /etc/profile

    # istioctl install --set profile=demo -y

    # clean up
    [ -f /tmp/movetodata_install_prepared ] && rm /tmp/movetodata_install_prepared
}


setup_movetodata()
{



    #image tags
    sudo docker tag $ARTIFACTORY_PATH/frontend                europe-west1-docker.pkg.dev/movetodata-bnp/movetodata-cr/frontend
    sudo docker tag $ARTIFACTORY_PATH/boson                   europe-west1-docker.pkg.dev/movetodata-bnp/movetodata-cr/boson
    sudo docker tag $ARTIFACTORY_PATH/julia                   europe-west1-docker.pkg.dev/movetodata-bnp/movetodata-cr/julia
    sudo docker tag $ARTIFACTORY_PATH/parler                  europe-west1-docker.pkg.dev/movetodata-bnp/movetodata-cr/parler
    sudo docker tag $ARTIFACTORY_PATH/funnel                  europe-west1-docker.pkg.dev/movetodata-bnp/movetodata-cr/funnel
    sudo docker tag $ARTIFACTORY_PATH/spark-history-server    europe-west1-docker.pkg.dev/movetodata-bnp/movetodata-cr/spark-history-server


    sudo docker tag 2461b2698dcd k8s.gcr.io/ingress-nginx/controller:v1.1.1
    sudo docker tag c41e9fcadf5a k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.1.1

    # remove duplicates
    sudo docker rmi $ARTIFACTORY_PATH/frontend
    sudo docker rmi $ARTIFACTORY_PATH/boson
    sudo docker rmi $ARTIFACTORY_PATH/julia
    sudo docker rmi $ARTIFACTORY_PATH/parler
    sudo docker rmi $ARTIFACTORY_PATH/funnel
    sudo docker rmi $ARTIFACTORY_PATH/spark-history-server

    # Now we configure kubernetes
     # this is special for onPremise
    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/metallb/namespace.yaml
    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/metallb/metallb.yaml
    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/metallb/metallb-configmap.yaml  # need to run manually

    sudo kubectl create -f $BUNDLE_DIR/deployments/configurations/metallb/ingress-nginx-controller-deploy.yaml
    sudo kubectl -n ingress-nginx annotate ingressclasses nginx ingressclass.kubernetes.io/is-default-class="true" --overwrite

    sudo kubectl create -f $BUNDLE_DIR/deployments/configurations/movetodata-namespace.yaml
    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/movetodata-secrets.yaml
    # sudo kubectl label namespace movetodata istio-injection=enabled

    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/boson-db-deployment.yaml
    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/boson-minio-storage.yaml 


    # Go in sleep loop until db started
    echo -n "waiting for database to start"
    while true
    do
    sudo kubectl -n movetodata get pods |grep boson-db|grep Running > /dev/null
    if [ $? == 0 ]; then
        echo "|"
        sleep 5
        # kubectl exec -it boson-db-5dd84575cf-z6l2j -- bin/bash -c "su - postgres -c \"psql -c 'DROP DATABASE boson;'\""
        sudo kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep boson-db` -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE boson;'\""
        sudo kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep boson-db` -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE kepler;'\""
        break
    fi
    echo -n "."
    sleep 5
    done

    #create dataset bucket in minio
    sudo kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep minio` -- /bin/bash -c "mkdir -p /storage/datasets"
    sudo kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep minio` -- /bin/bash -c "mkdir -p /storage/spark-streaming/checkpoint"

    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/boson-app-deployment.yaml

    # Go in sleep loop until boson started
    echo -n "waiting for backend/boson to start"
    while true
    do
    sudo kubectl -n movetodata get pods |grep -v boson-db|grep boson|grep Running > /dev/null
    if [ $? == 0 ]; then
        echo "|"
        break
    fi
    echo -n "."
    sleep 5
    done

    # safety net below
    sudo kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep boson-db` -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE boson;'\""
    sudo kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep boson-db` -- /bin/bash -c "su - postgres -c \"psql -c 'CREATE DATABASE kepler;'\""
    sudo kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep minio` -- /bin/bash -c "mkdir -p /storage/datasets"
    sudo kubectl -n movetodata exec -it `kubectl -n movetodata get pods |awk '{print $1}'|grep minio` -- /bin/bash -c "mkdir -p /storage/spark-streaming/checkpoint"


    # finally deploy frontend, parler etc
    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/frontend-deployment.yaml
    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/parler-deployment.yaml
    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/julia-deployment.yaml
    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/movetodata-spark-history-server.yaml

    sudo kubectl apply -f $BUNDLE_DIR/deployments/configurations/movetodata-ingress.yaml


    # setup spark
    sudo helm install -f $BUNDLE_DIR/deployments/spark/spark-helm-values.yaml movetodata $BUNDLE_DIR/deployments/spark/spark-operator-1.1.15.tgz --namespace movetodata
    sudo kubectl -n movetodata create serviceaccount spark
    sudo kubectl create clusterrolebinding spark-role --clusterrole=cluster-admin --user=system:serviceaccount:movetodata:spark
    # below is run build from cluster
    sudo kubectl create clusterrolebinding spark-role-default --clusterrole=cluster-admin --user=system:serviceaccount:movetodata:default

    # TODO : copy Fractal Templates
    
}

prepare()
{
    setup_container
    setup_kube_tools

    if [ $OFFLINE_INSTALL = "true" ]; then
        load_images
        # echo " not loading images for test"
    fi

    sudo echo ""
    sudo echo "**** : The preparation of setup has been completed successfully : ****"
    sudo echo ""
    sudo echo " !!! You could modify $BUNDLE_DIR/deployments/configurations/metallb/metallb-configmap.yaml  before reboot to add metallb IPs !!! "
    sudo echo ""
    sudo echo "Note : This setup requires a reboot at this stage. After reboot sudo $0 install"
    sudo echo ""

    sudo echo "Step1" >/tmp/movetodata_install_prepared
}




usage() {
    echo "$0 bundle|prepare|install"
}

opt="$1"

case "$opt" in
"bundle")
    bundle
    ;;
"prepare")
    # untar first in server
    # sudo mkdir /movetodata && sudo tar -C /movetodata -xf movetodata_bundle.tar.gz && sudo /movetodata/bundle/build_movetodata_offline.sh prepare
    prepare
    ;;
"install")
    install
    setup_movetodata
    sudo echo ""
    echo "**** : Setup completed successfully : ****"
    sudo echo ""
    sudo echo "!! Note !!: make sure to modify $BUNDLE_DIR/deployments/configurations/metallb/metallb-configmap.yaml and apply it, otherwise ingress will not work."
    sudo echo ""
    ;;
"setup_movetodata")
    setup_movetodata
    ;;
*)
    usage
    exit 1
    ;;
esac