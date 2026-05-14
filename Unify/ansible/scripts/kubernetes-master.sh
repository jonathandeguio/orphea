 Check if Kubernetes master node is initialized
if [ ! -d /etc/kubernetes ]; then
    echo "Initializing Kubernetes master node..."
    kubeadm init --pod-network-cidr=10.244.0.0/16
    mkdir -p $HOME/.kube
    sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
    sudo chown $(id -u):$(id -g) $HOME/.kube/config
else
    echo "Kubernetes master node is already initialized."
fi


# Install flannel CNI for pod networking if not already applied
if ! kubectl get pods --all-namespaces | grep -q flannel; then
    echo "Installing flannel CNI for pod networking..."
    kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml
else
    echo "Flannel CNI is already installed."
fi

echo "Kubernetes master node setup is complete."
