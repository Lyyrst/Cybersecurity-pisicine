#!/bin/bash

if [ "$#" -lt 1 ]; then
    echo "$0 FILE1 [FILE2 ...]"
    exit 1
fi

MODIFY=false
while getopts ":m" opt; do
    case ${opt} in
        m )
            MODIFY=true
            ;;
    \? )
        echo "$0 -m"
        exit 1
        ;;
    esac
done
shift $((OPTIND - 1))

if [ "$MODIFY" = false ] && [ "$#" -lt 1 ]; then
    echo "Usage: $0 [-modify|-m] FILE1 [FILE2 ...]"
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

if [ "$MODIFY" = true ]; then
    if ! command dialog &> /dev/null; then
        echo "Dialog is required to run this script"
        echo "sudo apt-get install dialog"
        exit 1
    fi

    files=$(find . -type f \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.bmp" -o -iname "*.gif" \) 2>/dev/null)
    if [[ -z "$files" ]]; then
        dialog --msgbox "No pictures found" 6 40
        exit 1
    fi

    file_list=()
    index=1
    while IFS= read -r file; do
        file_list+=("$index" "$file")
        index=$((index + 1))
    done <<< "$files"

    selected_index=$(dialog --title "Choose your picture" --menu "Select a valid picture:" 15 60 10 "${file_list[@]}" 3>&1 1>&2 2>&3)

    selected_file="${file_list[$((selected_index * 2 - 1))]}"

    creation_date=$(stat -c %y "$selected_file")
    exif_data=$(exiftool "$selected_file")

    tempfile=$(mktemp)
    echo "=== Creation Date: ===" > "$tempfile"
    echo "$creation_date" >> "$tempfile"
    echo -e "\n=== EXIF Data: ===" >> "$tempfile"
    echo "$exif_data" >> "$tempfile"

    if [[ -z "$creation_date" ]]; then
        creation_date="No creation date found."
    fi

    if [[ -z "$exif_data" ]]; then
        exif_data="No EXIF data found."
    fi

    # Display EXIF data in a text box
    dialog --title "Pictures Data" --textbox "$tempfile" 50 60
    rm "$tempfile"

    # Ask the user if they want to edit EXIF data or quit
    action=$(dialog --title "Edit EXIF Data" --menu "Do you want to edit a data field or quit?" 15 60 2 \
        1 "Edit EXIF Data" \
        2 "Quit" 3>&1 1>&2 2>&3)

    if [[ $? -ne 0 ]]; then
        echo "Error: operation canceled"
        exit 1
    fi

    if [ "$action" == "1" ]; then
        # Ask which EXIF field the user wants to modify
        field=$(dialog --inputbox "Enter the EXIF field to modify (e.g., Title, Author, etc.):" 8 60 3>&1 1>&2 2>&3)

        if [[ $? -ne 0 ]]; then
            echo "Error: operation canceled"
            exit 1
        fi

        # Ask for the new value for the EXIF field
        new_value=$(dialog --inputbox "Enter a new value for the EXIF field (leave blank to not change):" 8 60 3>&1 1>&2 2>&3)

        if [[ ! -z "$new_value" ]]; then
            exiftool -"$field"="$new_value" "$selected_file"
            dialog --msgbox "Update successful." 6 30
            # Fetch and display the updated EXIF data
            updated_exif_data=$(exiftool "$selected_file")
            updated_tempfile=$(mktemp)
            echo "$updated_exif_data" > "$updated_tempfile"
            
            dialog --title "Updated Pictures Data" --textbox "$updated_tempfile" 50 60
            rm "$updated_tempfile"
        else
            dialog --msgbox "No changes made." 6 30
        fi
    fi

else
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
fi
