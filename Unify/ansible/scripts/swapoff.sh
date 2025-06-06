#!/bin/bash

# Get the active swap device or file (Ubuntu might use /swapfile or /dev/sdX)
SWAP_DEVICE=$(swapon --show --noheadings | awk '{print $1}')

# Function to check if swap is already disabled
is_swap_disabled() {
    if [[ -z "$SWAP_DEVICE" ]]; then
        echo "Swap is already disabled."
        return 0
    else
        return 1
    fi
}

# Function to check if any swap entry exists in /etc/fstab
is_swap_entry_exists() {
    if grep -q "swap" /etc/fstab; then
        echo "A swap entry exists in /etc/fstab."
        return 0
    else
        echo "No swap entry found in /etc/fstab."
        return 1
    fi
}

# Function to check if swap entry is already commented out in /etc/fstab
is_swap_commented_out() {
    if grep -q "^#.*swap" /etc/fstab; then
        echo "Swap entry is already commented out in /etc/fstab."
        return 0
    else
        return 1
    fi
}

# Disable swap by modifying /etc/fstab and running swapoff
disable_swap() {
    echo "Disabling swap..."

    # Comment out the swap entry in /etc/fstab if it exists
    if is_swap_entry_exists && ! is_swap_commented_out; then
        echo "Commenting out swap entries in /etc/fstab..."
        sudo sed -i '/swap/s/^/#/' /etc/fstab
    fi

    # Disable swap immediately
    echo "Running swapoff -a..."
    sudo swapoff -a
}

# Main function to disable swap only if necessary
run() {
    if is_swap_disabled && (is_swap_entry_exists && is_swap_commented_out); then
        echo "Swap is already disabled and no changes are needed."
        exit 0
    else
        disable_swap
        echo "Swap has been disabled."
    fi
}

# Run the main function
run
