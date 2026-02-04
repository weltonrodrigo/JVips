# Linux arm64 (aarch64) build container for JVips
# Uses manylinux2014_aarch64 which provides CentOS 7 arm64 with glibc 2.17

FROM quay.io/pypa/manylinux2014_aarch64

# Set default build arguments.
ARG UNAME=jenkins
ARG UID=1000
ARG GID=1000

# Set default environment variables.
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
ENV YUM_OPTIONS="-y --setopt=skip_missing_names_on_install=False"

# manylinux2014 already has devtoolset and many build tools pre-installed
# Install additional dependencies
RUN yum install ${YUM_OPTIONS} \
    wget \
    git \
    cmake3 \
    python3 \
    python3-pip \
    ninja-build \
    patch \
    nasm \
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
    java-1.8.0-openjdk java-1.8.0-openjdk-devel

# Install meson via pip (more recent version than EPEL)
RUN pip3 install meson

# Install Maven (not available in manylinux repos)
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz | tar -xzf - -C /opt && \
    ln -s /opt/apache-maven-3.6.3/bin/mvn /usr/local/bin/mvn

# Symlink cmake3 to cmake if not already done
RUN if [ ! -f /usr/bin/cmake ]; then ln -s /usr/bin/cmake3 /usr/bin/cmake; fi

# Switch to a non-root user
RUN groupadd -g $GID -o $UNAME && \
    useradd -l -m -u $UID -g $GID -o -s /bin/bash -d /home/$UNAME $UNAME

USER $UNAME

CMD ["bash", "-l", "-ex", "build.sh", "--with-linux-arm64", "--without-linux-x86_64", "--without-w64", "--without-macos"]
