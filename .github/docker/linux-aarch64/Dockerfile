# Use manylinux2014 as base: CentOS 7 with glibc 2.17 and devtoolset-10 (GCC 10.x).
# This ensures compiled libraries are compatible with any runtime using glibc >= 2.17
# (RHEL 7+, UBI 8, etc.), and avoids broken SCL repos for aarch64 on CentOS 7 vault.
FROM quay.io/pypa/manylinux2014_aarch64

# Set default user (overridden in the command line with actual user).
ARG UNAME=builder
ARG UID=1000
ARG GID=1000

# Set default environment variables.
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk

# manylinux2014 already has vault repos configured, devtoolset-10 (GCC 10.x),
# cmake, git, autoconf, automake, libtool, and Python 3.
# Install EPEL and additional build dependencies.
RUN yum install -y epel-release && \
    yum install -y \
    gtk-doc \
    gobject-introspection gobject-introspection-devel \
    glib2 glib2-devel \
    openssl-devel \
    expat-devel \
    zlib-devel \
    libxml2-devel \
    xz xz-devel \
    mpfr-devel \
    gmp-devel \
    libmpc-devel \
    java-11-openjdk java-11-openjdk-devel \
    && yum clean all

# Install meson and ninja using one of the pre-installed Python interpreters
# (manylinux2014 has Python in /opt/python/, not in the default system PATH)
RUN /opt/python/cp310-cp310/bin/pip install meson ninja && \
    ln -s /opt/python/cp310-cp310/bin/meson /usr/local/bin/meson && \
    ln -s /opt/python/cp310-cp310/bin/ninja /usr/local/bin/ninja

# Install Maven (not in CentOS 7 repos for aarch64)
ARG MAVEN_VERSION=3.9.6
RUN curl -sL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz | tar -xz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/bin/mvn

# Switch to a non-root user.
RUN groupadd -g $GID -o $UNAME || true
RUN useradd -l -m -u $UID -g $GID -o -s /bin/bash -d /home/$UNAME $UNAME || true

USER $UNAME

CMD bash -l -ex build.sh --with-linux-aarch64 --without-linux --without-w64 --without-macos
