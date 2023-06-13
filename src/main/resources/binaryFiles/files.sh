#!/bin/bash

# Validate if the folder path is provided as an argument
if [ -z "$1" ]; then
  echo "Please provide the folder path as an argument."
  exit 1
fi

folder_path="$1"

# Validate if the folder exists
if [ ! -d "$folder_path" ]; then
  echo "Folder does not exist!"
  exit 1
fi

# Function to list files recursively
list_files_recursive() {
  local dir=$1
  local indent=$2
  local file

  # Loop through each file in the directory
  for file in "$dir"/*; do
    # Check if the file is a directory
    if [ -d "$file" ]; then
      echo "${indent}Directory: $file"
      list_files_recursive "$file" "$indent  "
    else
      echo "${indent}File: $file"
    fi
  done
}

# Call the function to list files recursively
list_files_recursive "$folder_path" ""