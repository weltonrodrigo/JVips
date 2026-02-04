FROM arm64v8/ubuntu:22.04

# Set default build arguments.
ARG NODE_VERSION=18.x

# Set default user (overriden in the command line with actual user).
ARG UNAME=builder
ARG UID=1000
ARG GID=1000

# Set default environment variables.
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-arm64
ENV DEBIAN_FRONTEND=noninteractive

# Install system dependencies.
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    git \
    cmake \
    maven \
    meson \
    ninja-build \
    patch \
    clang \
    gcc \
    g++ \
    nasm \
    autoconf \
    automake \
    libtool \
    diffutils \
    libssl-dev \
    libexpat1-dev \
    zlib1g-dev \
    libxml2-dev \
    xz-utils \
    liblzma-dev \
    libmpfr-dev \
    libgmp-dev \
    libmpc-dev \
    gtk-doc-tools \
    gobject-introspection \
    libgirepository1.0-dev \
    libglib2.0-dev \
    openjdk-11-jdk \
    python3 \
    python3-pip \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# Switch to a non-root user.
RUN groupadd -g $GID -o $UNAME || true
RUN useradd -l -m -u $UID -g $GID -o -s /bin/bash -d /home/$UNAME $UNAME || true

USER $UNAME

CMD bash -l -ex build.sh --with-linux-aarch64 --without-linux --without-w64 --without-macos
