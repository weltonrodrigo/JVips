FROM arm64v8/centos:7

# Set default user (overridden in the command line with actual user).
ARG UNAME=builder
ARG UID=1000
ARG GID=1000

# Set default environment variables.
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk
ENV PATH="${PATH}"
ENV YUM_OPTIONS="-y --setopt=skip_missing_names_on_install=False"

# Fix CentOS 7 EOL mirrors - use vault archive
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-*.repo && \
    sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*.repo

RUN yum install ${YUM_OPTIONS} centos-release-scl epel-release

# Fix SCL repo for EOL as well
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-SCLo-*.repo && \
    sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-SCLo-*.repo && \
    sed -i 's|# baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-SCLo-*.repo

# Install system dependencies.
RUN yum install ${YUM_OPTIONS} \
    wget \
    curl \
    git \
    cmake3 \
    devtoolset-7-toolchain \
    patch \
    clang \
    gcc \
    gcc-c++ \
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
    pkgconfig \
    make \
    && yum clean all

RUN ln -s /usr/bin/cmake3 /usr/bin/cmake

# Install meson and ninja via pip (not in CentOS 7 repos)
RUN pip3 install meson ninja

# Install Maven (not available in CentOS 7 repos for aarch64)
ARG MAVEN_VERSION=3.9.6
RUN curl -sL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz | tar -xz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/bin/mvn

# Switch to a non-root user.
RUN groupadd -g $GID -o $UNAME || true
RUN useradd -l -m -u $UID -g $GID -o -s /bin/bash -d /home/$UNAME $UNAME || true

RUN echo 'for scl in /opt/rh/*/enable; do source $scl; done' >> /etc/profile.d/rhscl.sh

USER $UNAME

CMD bash -l -ex build.sh --with-linux-aarch64 --without-linux --without-w64 --without-macos
