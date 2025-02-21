#!/bin/bash

RECURSIVE=false
RECURSIVE_DEPTH=5
SAVE_DIR="./data/"
IMG_TYPE="jpg|jpeg|gif|png|bmp"

help() {
    echo "$0 [-r] [-l N] [-p PATH] URL"
    echo " -r        : Use recursive mode to download images"
    echo " -l N      : Set N as the maximum recursion depth (default is 5)"
    echo " -p PATH   : Specify the directory to save downloaded images (default is ./data/)"
    exit 1
}

while getopts ":rl:p:" opt; do
    case ${opt} in
        r )
            RECURSIVE=true
            ;;
        l )
            if ! [[ $OPTARG =~ ^[0-9]+$ ]]; then
                echo "Error: -l requires a numeric argument."
                help
            fi
            RECURSIVE_DEPTH=$OPTARG
            ;;
        p )
            SAVE_DIR=$OPTARG
            if [ ! -d "$SAVE_DIR" ]; then
                mkdir -p "$SAVE_DIR"
                if [ $? -ne 0 ]; then
                    echo "Error: Unable to create directory '$SAVE_DIR'. Please check permissions."
                    exit 1
                fi
            fi
            ;;
        \? )
            help
            ;;
    esac
done

shift $((OPTIND -1))

URL=$1
if [ -z "$URL" ]; then
    help
fi

mkdir -p "$SAVE_DIR"

declare -A VISITED_URLS

download_imgs() {
    local url=$1
    local save_dir=$2
    local recursive_depth=$3

    if [ "$recursive_depth" -le 0 ]; then
        return
    fi

    if [[ ${VISITED_URLS[$url]} ]]; then
        echo "Already visited $url, skipping..."
        return
    fi

    VISITED_URLS[$url]=1

    html=$(curl -s -A "Mozilla/5.0" -L "$url" | tr -d '\000')

    img_urls=$(echo "$html" | grep -oE "https?://[^\"' ]+\.(jpg|jpeg|gif|png|bmp)(\?[^\"' ]*)?")

    if [ -z "$img_urls" ]; then
        echo "No direct image URLs found, trying <img src=...> pattern."
        img_urls=$(echo "$html" | grep -oE '<img [^>]*src="([^"]+\.(jpg|jpeg|gif|png|bmp)(\?[^"]*)?)"' | sed -E 's/.*src="([^"]+)".*/\1/')
    fi

    echo "Found image URLs:"
    echo "$img_urls"
    echo "============================="

    for img_url in $img_urls; do
        if [[ "$img_url" == //* ]]; then
            img_url="https:$img_url"
        fi

        if [[ "$img_url" =~ ^https?:// ]]; then
            filename=$(basename "${img_url%%\?*}")
            echo "Downloading $img_url as $filename..."
            curl -s -o "$save_dir/$filename" "$img_url"
        else
            echo "Invalid URL detected, skipping: $img_url"
        fi
    done

    if [ "$RECURSIVE" = true ] && [ "$recursive_depth" -gt 0 ]; then
        link_urls=$(echo "$html" | grep -oE 'href="[^"]+"' | sed -E 's/href="([^"]+)"/\1/' | grep -E "^http")
        echo "=== link urls ==="
        echo "$link_urls"
        echo "================="

        for link in $link_urls; do
            download_imgs "$link" "$save_dir" $((recursive_depth - 1))
        done
    fi
}


download_imgs "$URL" "$SAVE_DIR" "$RECURSIVE_DEPTH"

echo "Download completed. Images are saved in $SAVE_DIR."
