!/bin/bash

base_folder="src/components"

function check_pascal_case() {
  local text="$1"

  # Check if the first letter is uppercase and all subsequent words start with uppercase letters.
  if [[ $text =~ ^[[:upper:]][[:lower:][:digit:]_]*([[:upper:]][[:lower:][:digit:]_]*)*$ ]]; then
    return 0  # True
  else
    return 1  # False
  fi
}

while true; do
  # Get combined input from the user
  read -p "Enter FeatureName in PascalCase: " input

  # Check if the input is in PascalCase
  if ! check_pascal_case "$input"; then
    echo "FeatureName should be PascalCase"
    continue
  fi

  feature_name="$input"
  break
done


# Add feature_name as final parent in which feature files and folders will be created
base_folder="$base_folder/$feature_name"

# Create the modules directory
mkdir -p "$base_folder/modules"
mkdir -p "$base_folder/components"
mkdir -p "$base_folder/constants"
mkdir -p "$base_folder/utils"

# Create the files
touch "$base_folder/$feature_name.tsx"
touch "$base_folder/$feature_name.interfaces.ts"
touch "$base_folder/$feature_name.styles.ts"

echo "$feature_name created.
Move to appropriate folder as per need."
