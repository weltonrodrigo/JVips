FROM arm64v8/rockylinux:8

# Set default build arguments.
ARG NODE_VERSION=18.x

# Set default user (overriden in the command line with actual user).
ARG UNAME=builder
ARG UID=1000
ARG GID=1000

# Set default environment variables.
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk

# Install EPEL and enable PowerTools for additional packages
RUN dnf install -y epel-release && \
    dnf config-manager --set-enabled powertools

# Install system dependencies.
RUN dnf install -y \
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
    gcc-c++ \
    nasm \
    yasm \
    autoconf \
    automake \
    libtool \
    diffutils \
    openssl-devel \
    expat-devel \
    zlib-devel \
    libxml2-devel \
    xz xz-devel \
    mpfr-devel \
    gmp-devel \
    libmpc-devel \
    gtk-doc \
    gobject-introspection gobject-introspection-devel \
    glib2 glib2-devel \
    java-11-openjdk java-11-openjdk-devel \
    python3 \
    python3-pip \
    python3-devel \
    pkgconfig \
    make \
    && dnf clean all

# Switch to a non-root user.
RUN groupadd -g $GID -o $UNAME || true
RUN useradd -l -m -u $UID -g $GID -o -s /bin/bash -d /home/$UNAME $UNAME || true

USER $UNAME

CMD bash -l -ex build.sh --with-linux-aarch64 --without-linux --without-w64 --without-macos
