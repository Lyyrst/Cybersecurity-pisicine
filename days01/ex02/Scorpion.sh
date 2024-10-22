#!/bin/bash

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 FILE1 [FILE2 ...]"
    exit 1
fi

supported_extensions=("png" "jpg" "jpeg" "bmp" "gif")

has_supported_extension() {
    local file="$1"
    for ext in "${supported_extensions[@]}"; do
        if [[ "$file" == *.$ext ]]; then
            return 0
        fi
    done
    return 1
}

for file in "$@"; do
    if [[ ! -f "$file" ]]; then
        echo "File '$file' does not exist."
        continue
    fi

    if has_supported_extension "$file"; then
        echo "Processing '$file':"

        creation_date=$(stat -c %y "$file" 2>/dev/null)
        echo "  Creation Date: $creation_date"

        exif_data=$(exiftool "$file")
        if [ -z "$exif_data" ]; then
            echo "  No EXIF data found."
        else
            echo "  EXIF Data:"
            echo "$exif_data"
        fi

        echo "----------------------------------------"
    else
        echo "File '$file' is not a supported image type."
    fi
done
