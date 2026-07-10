#!/bin/bash

# Variables
USERNAME="movetodata"
PASSWORD="movetodata2024"
SUDOERS_FILE="/etc/sudoers.d/$USERNAME"

# Check if user 'movetodata' exists, if not create the user
if id "$USERNAME" &>/dev/null; then
    echo "User '$USERNAME' already exists."
else
    echo "Creating user '$USERNAME'..."
    sudo useradd -m $USERNAME
    echo "$USERNAME:$PASSWORD" | sudo chpasswd
    echo "User '$USERNAME' created with password '$PASSWORD'."
fi

# Add the user to sudoers if not already present
if sudo grep -q "^$USERNAME" $SUDOERS_FILE; then
    echo "User '$USERNAME' is already in the sudoers file."
else
    echo "Adding user '$USERNAME' to sudoers..."
    echo "$USERNAME ALL=(ALL) NOPASSWD:ALL" | sudo tee $SUDOERS_FILE > /dev/null
    sudo chmod 0440 $SUDOERS_FILE
    echo "User '$USERNAME' added to sudoers with NOPASSWD privilege."
fi

echo "Setup complete."
