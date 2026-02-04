#!/bin/bash

set -e

command -v brew || {
    echo >&2 "Homebrew missing."
    exit 1
}

command -v cmake || {
    brew install cmake
}

command -v pkg-config || {
    brew install pkg-config
}

# Install latest vips from Homebrew.
# The JNI bindings use stable API that is compatible across 8.12+.
command -v vipsthumbnail || {
    brew install vips || {
        echo >&2 "Vips not installed."
        exit 1
    }
}
echo "vips $(vipsthumbnail --vips-version) installed."
