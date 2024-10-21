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
            RECURSIVE_DEPTH=$OPTARG
            ;;
        p )
            SAVE_DIR=$OPTARG
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

    if [[ ${VISITED_URLS[$url]} ]]; then
        echo "Already visited $url, skipping..."
        return
    fi

    VISITED_URLS[$url]=1

    html=$(curl -s "$url" | tr -d '\000')

    img_urls=$(echo "$html" | grep -oP "(http[s]?:\/\/[^\"']*\.(?:$IMG_TYPE))")
    for img_url in $img_urls; do
        echo "Downloading $img_url..."
        curl -s -o "$save_dir/$(basename "$img_url")" "$img_url"
    done

    if [ "$RECURSIVE" = true ] && [ "$recursive_depth" -gt 0 ]; then
        link_urls=$(echo "$html" | grep -oP "href=['\"]\K[^\"']+(?=['\"])" | grep -E "^http")
        for link in $link_urls; do
            download_imgs "$link" "$save_dir" $((recursive_depth - 1))
        done
    fi
}

download_imgs "$URL" "$SAVE_DIR" "$RECURSIVE_DEPTH"

echo "Download completed. Images are saved in $SAVE_DIR."
