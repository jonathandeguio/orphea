!/bin/bash

# Check if the user provided a new hostname as an argument
if [ -z "$1" ]; then
    echo "Usage: $0 <new-hostname>"
    exit 1
fi

NEW_HOSTNAME="$1"
CURRENT_HOSTNAME=$(hostname)

# Function to update the hostname using hostnamectl
update_hostname() {
    echo "Current hostname: $CURRENT_HOSTNAME"
    
    # Check if the new hostname is the same as the current one
    if [ "$NEW_HOSTNAME" == "$CURRENT_HOSTNAME" ]; then
        echo "The new hostname is the same as the current hostname. No changes are required."
        exit 0
    else
        echo "Updating hostname to $NEW_HOSTNAME..."
        sudo hostnamectl set-hostname "$NEW_HOSTNAME"
        if [ $? -eq 0 ]; then
            echo "Hostname successfully changed to $NEW_HOSTNAME."
        else
            echo "Failed to change hostname."
            exit 1
        fi
    fi
}

# Function to update /etc/hosts
update_hosts_file() {
    echo "Updating /etc/hosts..."

    # Replace the old hostname with the new one in /etc/hosts
    sudo sed -i "s/$CURRENT_HOSTNAME/$NEW_HOSTNAME/g" /etc/hosts

    # Ensure that the new hostname is properly reflected in /etc/hosts
    if ! grep -q "$NEW_HOSTNAME" /etc/hosts; then
        echo "Adding $NEW_HOSTNAME to /etc/hosts..."
        echo "127.0.0.1   $NEW_HOSTNAME" | sudo tee -a /etc/hosts
    fi

    echo "/etc/hosts has been updated."
}

# Main function to change the hostname
change_hostname() {
    update_hostname
    update_hosts_file
    echo "Hostname change complete. Please reboot your system for the changes to take full effect."
}

# Run the main function
change_hostname
