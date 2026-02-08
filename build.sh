#!/bin/bash

set -e
set -x

BASEDIR="$(pwd)"

# Detect native architecture
detect_arch() {
    local arch=$(uname -m)
    case "$arch" in
        x86_64|amd64) echo "x86_64" ;;
        aarch64|arm64) echo "aarch64" ;;
        *) echo "x86_64" ;;  # fallback
    esac
}

NATIVE_ARCH=$(detect_arch)

BUILD_LINUX_X86_64=0
BUILD_LINUX_AARCH64=0
BUILD_LINUX_AARCH64_SVE2=0
BUILD_WIN64=0
BUILD_MACOS=0
DIST=0
DEBUG=0
JOBS=8
BUILD_TYPE=Release
RUN_TEST=1
RUN_BENCHMARK=0
MAVEN_ARGS="--batch-mode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"

while true; do
  case "$1" in
    --with-w64 ) BUILD_WIN64=1; shift;;
    --with-linux-x86_64 ) BUILD_LINUX_X86_64=1; shift;;
    --with-linux-aarch64 | --with-linux-arm64 ) BUILD_LINUX_AARCH64=1; shift;;
    --with-linux-aarch64-sve2 ) BUILD_LINUX_AARCH64_SVE2=1; shift;;
    --with-linux )
        # Auto-detect based on native arch
        if [ "$NATIVE_ARCH" = "aarch64" ]; then
            BUILD_LINUX_AARCH64=1
        else
            BUILD_LINUX_X86_64=1
        fi
        shift;;
    --with-macos ) BUILD_MACOS=1; shift;;
    --without-w64 ) BUILD_WIN64=0; shift;;
    --without-linux-x86_64 ) BUILD_LINUX_X86_64=0; shift;;
    --without-linux-aarch64 | --without-linux-arm64 ) BUILD_LINUX_AARCH64=0; shift;;
    --without-linux-aarch64-sve2 ) BUILD_LINUX_AARCH64_SVE2=0; shift;;
    --without-linux ) BUILD_LINUX_X86_64=0; BUILD_LINUX_AARCH64=0; BUILD_LINUX_AARCH64_SVE2=0; shift;;
    --without-macos ) BUILD_MACOS=0; shift;;
    --skip-test ) RUN_TEST=0; shift;;
    --run-benchmark ) RUN_BENCHMARK=1; shift;;
    --dist ) DIST=1; shift;;
    --minimal ) MAVEN_ARGS="${MAVEN_ARGS} -Pminimal"; shift;;
    --debug ) DEBUG=1; shift ;;
    --jobs ) JOBS="$2"; shift 2 ;;
    -- ) shift; break ;;
    * ) break ;;
  esac
done

if [ ${JOBS} -le 0 ]; then
    JOBS=1
fi

if [ ${DEBUG} -eq 1 ]; then
    BUILD_TYPE=Debug
fi

export JOBS
export DEBUG
export BUILDDIR="${BASEDIR}/build"

CMAKE_BIN=$(which cmake3 || which cmake)
PYTHON_BIN=$(which python3 || which python)

# Clear the build dir before anything else in the CI
if [ "${CI}" = "true" ]; then
    rm -rf "${BUILDDIR}"
fi

# Copy maven dependencies for some tests
mkdir -p "${BUILDDIR}"/artifacts/
mvn ${MAVEN_ARGS} dependency:copy-dependencies -DoutputDirectory="${BUILDDIR}"/artifacts/

# Create the resource directory where all native libraries will be copied.
mkdir -p "${BUILDDIR}"/all/

source lib/VERSIONS
VERSION="${VIPS_VERSION}-$(git rev-parse --short HEAD)"

# Function to generate enums from GIR file
generate_enums() {
    local GIR_FILE="$1"
    echo "Generating enums from GIR file: ${GIR_FILE}"
    (
        cd script/enum-generator
        ${PYTHON_BIN} -m venv venv
        VENV_PIP_BIN="./venv/bin/pip"
        VENV_PYTHON_BIN="./venv/bin/python"
        ${VENV_PIP_BIN} install -r requirements.txt
        ${VENV_PYTHON_BIN} EnumGenerator.py --gir "${GIR_FILE}"
    )
}

##########################
###### Build Linux #######
##########################

# Function to build Linux for a specific architecture
build_linux() {
    local ARCH="$1"
    local HOST_FLAG="$2"
    local TOOLCHAIN_FILE="$3"
    local TARGET_DIR="linux-${ARCH}"

    export CC=gcc
    export CXX=g++
    export CPP=cpp
    export RANLIB=ranlib
    export HOST="${HOST_FLAG}"
    export TARGET="${TARGET_DIR}"
    export PREFIX="${BUILDDIR}/${TARGET}"/inst/
    export TOOLCHAIN="${BASEDIR}/${TOOLCHAIN_FILE}"
    if [ "$ARCH" = "x86_64" ]; then
        export PKG_CONFIG_PATH=/usr/lib64/pkgconfig
    else
        export PKG_CONFIG_PATH=/usr/lib/pkgconfig
    fi

    mkdir -p "${BUILDDIR}/${TARGET}"/JVips
    rm -rf "${BUILDDIR}/${TARGET}"/JVips/*
    pushd "${BUILDDIR}/${TARGET}"/JVips

    # Phase 1: Build native libraries including libvips with meson (generates GIR)
    ${CMAKE_BIN} "${BASEDIR}" -DWITH_LIBHEIF=ON -DWITH_LIBSPNG=ON -DCMAKE_TOOLCHAIN_FILE="${TOOLCHAIN}" -DCMAKE_BUILD_TYPE=${BUILD_TYPE}

    # Build just libvips first to generate the GIR file
    make -j ${JOBS} libvips || {
        echo "Linux ${ARCH} libvips build failed"
        exit 1
    }
    popd

    # Generate enums from the built GIR file (only on first build)
    GIR_FILE="${PREFIX}/share/gir-1.0/Vips-8.0.gir"
    if [ -f "${GIR_FILE}" ]; then
        generate_enums "${GIR_FILE}"
    else
        echo "WARNING: GIR file not found at ${GIR_FILE}"
        echo "Enum generation skipped - you may need to run it manually"
    fi

    # Phase 2: Build JVips JNI wrapper
    pushd "${BUILDDIR}/${TARGET}"/JVips
    make -j ${JOBS} || {
        echo "Linux ${ARCH} JVips build failed"
        exit 1
    }
    popd

    # Copy to architecture-specific output directory
    mkdir -p "${BUILDDIR}/${TARGET_DIR}/"

    LIBS="JVips/src/main/c/libJVips.so"
    if [ ${RUN_TEST} -gt 0 ]; then
        LIBS+=" JVips/src/test/c/libJVipsTest.so"
    fi

    for LIB in $LIBS; do
        cp "${BUILDDIR}/${TARGET}/${LIB}" "${BUILDDIR}/${TARGET_DIR}/"
    done
    cp "${BUILDDIR}/${TARGET}/inst/lib/"*.so* "${BUILDDIR}/${TARGET_DIR}/"
    # Also copy from lib64 (meson installs some libs there on x86_64)
    if [ -d "${BUILDDIR}/${TARGET}/inst/lib64" ]; then
        cp "${BUILDDIR}/${TARGET}/inst/lib64/"*.so* "${BUILDDIR}/${TARGET_DIR}/" 2>/dev/null || true
    fi
}

if [ ${BUILD_LINUX_X86_64} -gt 0 ]; then
    build_linux "x86_64" "--host=x86_64-pc-linux" "Toolchain-linux-x86_64.cmake"
fi

if [ ${BUILD_LINUX_AARCH64} -gt 0 ]; then
    build_linux "aarch64" "--host=aarch64-linux-gnu" "Toolchain-linux-aarch64.cmake"
fi

if [ ${BUILD_LINUX_AARCH64_SVE2} -gt 0 ]; then
    build_linux "aarch64-sve2" "--host=aarch64-linux-gnu" "Toolchain-linux-aarch64-sve2.cmake"
fi

##########################
###### Build Win64 #######
##########################

if [ ${BUILD_WIN64} -gt 0 ]; then
    WIN_TARGET_DIR="windows-x86_64"

    export MINGW=x86_64-w64-mingw32
    export CC=${MINGW}-gcc
    export CXX=${MINGW}-g++
    export CPP=${MINGW}-cpp
    export RANLIB=${MINGW}-ranlib
    export PATH=/usr/${MINGW}/bin:${PATH}

    export HOST="--host=x86_64-w64-mingw32"
    export TARGET=w64
    export PREFIX="${BUILDDIR}/${TARGET}"/inst/
    export TOOLCHAIN="${BASEDIR}"/Toolchain-x86_64-w64-mingw32.cmake
    export PKG_CONFIG_PATH=/usr/x86_64-w64-mingw32/sys-root/mingw/lib/pkgconfig/
    export PKG_CONFIG="x86_64-w64-mingw32-pkg-config"

    mkdir -p "${BUILDDIR}/${TARGET}"/JVips
    rm -rf "${BUILDDIR}/${TARGET}"/JVips/*
    pushd "${BUILDDIR}/${TARGET}"/JVips
    ${CMAKE_BIN} "${BASEDIR}" -DCMAKE_TOOLCHAIN_FILE="${TOOLCHAIN}" -DCMAKE_BUILD_TYPE=${BUILD_TYPE}
    make -j ${JOBS} || {
        echo "Windows 64 JVips build failed"
        exit 1
    }
    popd

    # Copy to architecture-specific output directory
    mkdir -p "${BUILDDIR}/${WIN_TARGET_DIR}/"

    LIBS="inst/lib/libimagequant.dll JVips/src/main/c/JVips.dll"
    if [ ${RUN_TEST} -gt 0 ]; then
        LIBS+=" JVips/src/test/c/JVipsTest.dll"
    fi

    for LIB in $LIBS; do
        cp "${BUILDDIR}/${TARGET}/${LIB}" "${BUILDDIR}/${WIN_TARGET_DIR}/"
    done
fi

##########################
###### Build macOS #######
##########################

if [ ${BUILD_MACOS} -gt 0 ]; then
    # Detect macOS architecture
    MACOS_ARCH=$(detect_arch)
    MACOS_TARGET_DIR="darwin-${MACOS_ARCH}"

    export HOST=""
    export TARGET=macOS
    export PREFIX="${BUILDDIR}/${TARGET}"/inst/
    export TOOLCHAIN="${BASEDIR}"/Toolchain-macOS.cmake

    # On macOS, generate enums from homebrew's GIR file before building
    if command -v brew &> /dev/null; then
        BREW_PREFIX=$(brew --prefix)
        GIR_FILE="${BREW_PREFIX}/share/gir-1.0/Vips-8.0.gir"
        if [ -f "${GIR_FILE}" ]; then
            generate_enums "${GIR_FILE}"
        else
            echo "WARNING: GIR file not found at ${GIR_FILE}"
            echo "Please ensure vips is installed: brew install vips"
            echo "Enum generation skipped"
        fi
    else
        echo "WARNING: Homebrew not found, cannot locate GIR file"
        echo "Enum generation skipped"
    fi

    mkdir -p "${BUILDDIR}/${TARGET}"/JVips
    rm -rf "${BUILDDIR}/${TARGET}"/JVips/*
    pushd "${BUILDDIR}/${TARGET}/JVips"
    ${CMAKE_BIN} "${BASEDIR}" -DWITH_LIBHEIF=ON -DWITH_LIBSPNG=ON -DCMAKE_TOOLCHAIN_FILE="${TOOLCHAIN}" -DCMAKE_BUILD_TYPE=${BUILD_TYPE}
    make -j ${JOBS} || {
        echo "macOS JVips build failed"
        exit 1
    }
    popd

    # Copy to architecture-specific output directory
    mkdir -p "${BUILDDIR}/${MACOS_TARGET_DIR}/"

    LIBS="JVips/src/main/c/libJVips.dylib"
    if [ ${RUN_TEST} -gt 0 ]; then
        LIBS+=" JVips/src/test/c/libJVipsTest.dylib"
    fi

    for LIB in $LIBS; do
        cp "${BUILDDIR}/${TARGET}/${LIB}" "${BUILDDIR}/${MACOS_TARGET_DIR}/"
    done

fi

mvn ${MAVEN_ARGS} -DnewVersion=${VERSION} versions:set
mvn ${MAVEN_ARGS} -DskipTests clean package
mvn ${MAVEN_ARGS} versions:revert

if [ ${RUN_TEST} -gt 0 ]; then
    mvn ${MAVEN_ARGS} surefire:test@utest
fi

if [ ${RUN_BENCHMARK} -gt 0 ]; then
    mvn ${MAVEN_ARGS} surefire:test@benchmark
    if [ ${BUILD_LINUX} -gt 0 ]; then
         "${BUILDDIR}/linux/JVips/src/test/c/benchmark/SimpleBenchmark" "${BASEDIR}/src/test/resources/in_vips.jpg"
    fi
fi

if [ ${DIST} -gt 0 ]; then
    if [ ${BUILD_LINUX_X86_64} -gt 0 ]; then
       tar -czvf "JVips-linux-x86_64.tar.gz" JVips.jar -C "${BUILDDIR}"/linux-x86_64/inst/ bin lib include share
    fi
    if [ ${BUILD_LINUX_AARCH64} -gt 0 ]; then
       tar -czvf "JVips-linux-aarch64.tar.gz" JVips.jar -C "${BUILDDIR}"/linux-aarch64/inst/ bin lib include share
    fi
    if [ ${BUILD_LINUX_AARCH64_SVE2} -gt 0 ]; then
       tar -czvf "JVips-linux-aarch64-sve2.tar.gz" JVips.jar -C "${BUILDDIR}"/linux-aarch64-sve2/inst/ bin lib include share
    fi
fi

if [ "${CI}" = "true" ]; then
    # Archive all platform-specific libraries
    LIBS_ARCHIVE=""
    for PLATFORM_DIR in linux-x86_64 linux-aarch64 linux-aarch64-sve2 darwin-x86_64 darwin-aarch64 windows-x86_64; do
        if [ -d "${BUILDDIR}/${PLATFORM_DIR}" ]; then
            LIBS_ARCHIVE="${LIBS_ARCHIVE} ${PLATFORM_DIR}"
        fi
    done
    if [ -n "${LIBS_ARCHIVE}" ]; then
        (cd "${BUILDDIR}" && tar -czvf "${BASEDIR}/JVips-libs.tar.gz" ${LIBS_ARCHIVE})
    fi
fi
