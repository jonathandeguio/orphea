#!/bin/bash

# Variables (modify these as needed)
NFS_SERVER="100.101.1.250"
NFS_SHARE="/JST000AK18_1"
MOUNT_POINT="/bosler"
IP_ADDRESS="100.101.1.8/24"
NETPLAN_FILE="/etc/netplan/99-netcfg-vmware.yaml"  # Path to the Netplan config file
FSTAB_ENTRY="$NFS_SERVER:$NFS_SHARE $MOUNT_POINT nfs rw,tcp,rsize=32768,wsize=32768,hard,intr,_netdev 0 0"

# Function to install necessary packages
install_nfs_package() {
    echo "[INFO] Checking if NFS package is installed..."
    if ! dpkg -l | grep -qw nfs-common; then
        echo "[INFO] NFS package not found, installing..."
        sudo apt-get -qq -y update && sudo apt install -y nfs-common
        if [ $? -eq 0 ]; then
            echo "[SUCCESS] NFS package installed successfully."
        else
            echo "[ERROR] Failed to install NFS package."
            exit 1
        fi
    else
        echo "[INFO] NFS package already installed."
    fi
}

# Function to find a network interface without an IPv4 address
find_blank_interface() {
    echo "[INFO] Looking for a network interface without an IPv4 address..."

    NET_INTERFACE=$(ip -o link show | awk '$9 == "UP" {print $2}' | sed 's/://g' | while read -r iface; do
        if ! ip addr show "$iface" | grep -q "inet "; then
            echo "$iface"
            break
        fi
    done)

    if [ -z "$NET_INTERFACE" ]; then
        echo "[ERROR] No available network interface without an IP address found."
        exit 1
    else
        echo "[SUCCESS] Found available interface: $NET_INTERFACE"
    fi
}

# Function to update Netplan configuration with static IP and disable DHCP6
update_netplan_config() {
    echo "[INFO] Updating Netplan configuration for interface $NET_INTERFACE..."

    # Backup the original Netplan file if it doesn't already exist
    if [ ! -f "${NETPLAN_FILE}.bak" ]; then
        echo "[INFO] Creating a backup of the Netplan configuration file..."
        sudo cp "$NETPLAN_FILE" "${NETPLAN_FILE}.bak"
        if [ $? -eq 0 ]; then
            echo "[SUCCESS] Backup created at ${NETPLAN_FILE}.bak."
        else
            echo "[ERROR] Failed to create a backup of the Netplan file."
            exit 1
        fi
    else
        echo "[INFO] Backup of Netplan already exists, skipping backup."
    fi

    # Check if the interface ens256 already has an IP address entry
    if grep -q "$IP_ADDRESS" "$NETPLAN_FILE"; then
        echo "[INFO] IP $IP_ADDRESS already exists for interface $NET_INTERFACE."
    else
        echo "[INFO] Adding static IP $IP_ADDRESS and disabling DHCP6 for $NET_INTERFACE..."

        # Append the new address under ens256
        sudo sed -i "/$NET_INTERFACE:/a\      addresses:\n        - $IP_ADDRESS" "$NETPLAN_FILE"
        sudo sed -i "/$NET_INTERFACE:/,/dhcp6/s/dhcp6: yes/dhcp6: no/" "$NETPLAN_FILE"
        sudo sed -i "/$NET_INTERFACE:/,/dhcp4/s/dhcp4: yes/dhcp4: no/" "$NETPLAN_FILE"

        if grep -q "$IP_ADDRESS" "$NETPLAN_FILE"; then
            echo "[SUCCESS] Static IP $IP_ADDRESS added to interface $NET_INTERFACE."
        else
            echo "[ERROR] Failed to add IP address $IP_ADDRESS to $NET_INTERFACE in Netplan."
            exit 1
        fi
    fi

    # Apply the Netplan configuration
    echo "[INFO] Applying Netplan configuration..."
    sudo netplan apply
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] Netplan configuration applied successfully."
    else
        echo "[ERROR] Failed to apply Netplan configuration."
        exit 1
    fi
}

# Function to check if the directory exists and create it if necessary
check_mount_directory() {
    echo "[INFO] Checking if mount directory $MOUNT_POINT exists..."
    if [ ! -d "$MOUNT_POINT" ]; then
        echo "[INFO] Creating mount directory at $MOUNT_POINT..."
        sudo mkdir -p "$MOUNT_POINT"
        if [ $? -eq 0 ]; then
            echo "[SUCCESS] Mount directory created at $MOUNT_POINT."
        else
            echo "[ERROR] Failed to create mount directory."
            exit 1
        fi
    else
        echo "[INFO] Mount directory $MOUNT_POINT already exists."
    fi
}

# Function to update /etc/fstab if not already present
update_fstab_entry() {
    echo "[INFO] Checking if NFS entry is already present in /etc/fstab..."
    if grep -q "$NFS_SERVER:$NFS_SHARE" /etc/fstab; then
        echo "[INFO] NFS entry already exists in /etc/fstab."
    else
        echo "[INFO] Adding NFS entry to /etc/fstab..."
        echo "$FSTAB_ENTRY" | sudo tee -a /etc/fstab
        if [ $? -eq 0 ]; then
            echo "[SUCCESS] NFS entry added to /etc/fstab."
        else
            echo "[ERROR] Failed to add NFS entry to /etc/fstab."
            exit 1
        fi
    fi
}

# Function to test connection to NFS server before mounting
test_nfs_connection() {
    echo "[INFO] Testing connection to NFS server $NFS_SERVER..."
    ping -c 3 "$NFS_SERVER"
    if [ $? -eq 0 ]; then
        echo "[SUCCESS] Connection to NFS server $NFS_SERVER is successful."
    else
        echo "[ERROR] Unable to reach NFS server $NFS_SERVER. Please check your network configuration."
        exit 1
    fi
}

# Function to mount the NFS share
mount_nfs_share() {
    echo "[INFO] Mounting NFS share..."
    sudo mount -a
    if mountpoint -q "$MOUNT_POINT"; then
        echo "[SUCCESS] NFS share mounted successfully at $MOUNT_POINT."
    else
        echo "[ERROR] Failed to mount NFS share at $MOUNT_POINT. Connection timed out."
        exit 1
    fi
}

# Main execution sequence
echo "[INFO] Starting NFS setup process for new environment..."

install_nfs_package
find_blank_interface
update_netplan_config
check_mount_directory
update_fstab_entry
test_nfs_connection  # Check the connection before attempting to mount
mount_nfs_share

echo "[INFO] NFS setup process completed for the new environment."
