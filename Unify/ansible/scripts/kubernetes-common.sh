!/bin/bash

# Install dependencies if not installed
echo "Checking for required dependencies..."
if ! dpkg -l | grep -qw "apt-transport-https"; then
    echo "Installing apt-transport-https, ca-certificates, and curl..."
    apt-get update
    apt-get install -qq -y apt-transport-https ca-certificates curl
else
    echo "Required dependencies are already installed."
fi

# Add Kubernetes GPG key if not present
if [ ! -f /usr/share/keyrings/kubernetes-archive-keyring.gpg ]; then
    echo "Adding Kubernetes GPG key..."
    curl -fsSLo /usr/share/keyrings/kubernetes-archive-keyring.gpg https://dl.k8s.io/apt/doc/apt-key.gpg
else
    echo "Kubernetes GPG key is already present."
fi

# Add Kubernetes apt repository if not added
if [ ! -f /etc/apt/sources.list.d/kubernetes.list ]; then
    echo "Adding Kubernetes apt repository..."
    echo "deb [signed-by=/usr/share/keyrings/kubernetes-archive-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.31/deb/ /" | sudo tee /etc/apt/sources.list.d/kubernetes.list
else
    echo "Kubernetes apt repository is already added."
fi

# Update package index and install Kubernetes packages if not installed
if ! dpkg -l | grep -qw kubelet || ! dpkg -l | grep -qw kubeadm || ! dpkg -l | grep -qw kubectl; then
    echo "Updating package index and installing Kubernetes packages..."
    apt-get update
    apt-get install -qq -y kubelet kubeadm kubectl
    apt-mark hold kubelet kubeadm kubectl
else
    echo "Kubernetes packages are already installed."
fi

# Check if kernel modules are loaded and enable if necessary
echo "Checking if kernel modules are loaded..."
if ! lsmod | grep -q overlay || ! lsmod | grep -q br_netfilter; then
    echo "Loading required kernel modules..."
    cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF
    modprobe overlay
    modprobe br_netfilter
    echo "Kernel modules loaded."
else
    echo "Kernel modules are already loaded."
fi

# Configure sysctl for Kubernetes networking if not already configured
SYSCTL_FILE="/etc/sysctl.d/k8s.conf"
if [ ! -f "$SYSCTL_FILE" ]; then
    echo "Configuring sysctl for Kubernetes networking..."
    cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward = 1
EOF
    sysctl --system
    echo "Sysctl configuration applied."
else
    echo "Sysctl for Kubernetes networking is already configured."
fi