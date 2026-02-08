#!/bin/bash
#
# Multiarch build script for JVips
#
# Builds all platform variants and assembles them into a single JAR:
#   - linux-x86_64       (SSE2/AVX2 baseline)
#   - linux-aarch64      (ARM64 NEON baseline)
#   - linux-aarch64-sve2 (ARM64 SVE2 for Azure Cobalt 100 / Neoverse N2)
#   - darwin-aarch64     (macOS ARM64, JNI wrapper only)
#
# Prerequisites:
#   - Docker contexts: docker-runtime (x86_64 VM), docker-arm64 (aarch64 VM)
#   - SSH aliases: dockervm (x86_64), dockervmarm (aarch64)
#   - macOS: Homebrew with vips 8.18.0, Java 8 (Corretto/Adoptium)
#   - Azure VMs running (start with: az vm start -n <name> -g maquinas-virtuais --subscription SPIA)
#
# Usage:
#   ./build-multiarch.sh              # build all 4 variants
#   ./build-multiarch.sh --skip-test  # build without tests
#   ./build-multiarch.sh --only linux-x86_64 linux-aarch64  # build specific variants

set -euo pipefail

BASEDIR="$(cd "$(dirname "$0")" && pwd)"
cd "$BASEDIR"

# --- Configuration ---
DOCKER_CTX_X86=docker-runtime
DOCKER_CTX_ARM=docker-arm64
DOCKER_IMG_X86=jvips-build-x86_64
DOCKER_IMG_ARM=jvips-build-arm64
SSH_X86=dockervm
SSH_ARM=dockervmarm
REMOTE_DIR_X86=/home/torres/jvips-neon
REMOTE_DIR_ARM=/home/azureuser/jvips-neon
JAVA8_HOME="${JAVA8_HOME:-$(find ~/Library/Java/JavaVirtualMachines/corretto-1.8.* -maxdepth 0 2>/dev/null | sort -V | tail -1)/Contents/Home}"
DOCKERFILE=.github/docker/linux/Dockerfile
SKIP_TEST="--skip-test"
BUILD_X86=1
BUILD_ARM=1
BUILD_SVE2=1
BUILD_MACOS=1

# --- Parse arguments ---
ONLY_MODE=0
ONLY_TARGETS=()

while [[ $# -gt 0 ]]; do
    case "$1" in
        --skip-test)  SKIP_TEST="--skip-test"; shift ;;
        --with-test)  SKIP_TEST=""; shift ;;
        --only)       ONLY_MODE=1; shift ;;
        --help|-h)
            echo "Usage: $0 [--skip-test] [--with-test] [--only <targets...>]"
            echo ""
            echo "Targets: linux-x86_64, linux-aarch64, linux-aarch64-sve2, darwin-aarch64"
            echo ""
            echo "Environment variables:"
            echo "  JAVA8_HOME   Path to Java 8 JDK (default: auto-detect Corretto 8)"
            exit 0
            ;;
        linux-x86_64|linux-aarch64|linux-aarch64-sve2|darwin-aarch64)
            ONLY_TARGETS+=("$1"); shift ;;
        *)
            echo "Unknown argument: $1" >&2; exit 1 ;;
    esac
done

if [[ $ONLY_MODE -eq 1 && ${#ONLY_TARGETS[@]} -gt 0 ]]; then
    BUILD_X86=0; BUILD_ARM=0; BUILD_SVE2=0; BUILD_MACOS=0
    for t in "${ONLY_TARGETS[@]}"; do
        case "$t" in
            linux-x86_64)       BUILD_X86=1 ;;
            linux-aarch64)      BUILD_ARM=1 ;;
            linux-aarch64-sve2) BUILD_SVE2=1 ;;
            darwin-aarch64)     BUILD_MACOS=1 ;;
        esac
    done
fi

# --- Helpers ---
log()  { echo "=== $(date +%H:%M:%S) $*"; }
fail() { echo "FATAL: $*" >&2; exit 1; }

check_docker_ctx() {
    local ctx=$1 name=$2
    if ! DOCKER_CONTEXT="$ctx" docker ps >/dev/null 2>&1; then
        fail "$name Docker VM not reachable (context: $ctx). Start it first."
    fi
}

ensure_docker_image() {
    local ctx=$1 img=$2
    if ! DOCKER_CONTEXT="$ctx" docker images --format '{{.Repository}}' | grep -q "^${img}$"; then
        log "Building Docker image $img (context: $ctx)..."
        DOCKER_CONTEXT="$ctx" docker build -t "$img" -f "$DOCKERFILE" "$BASEDIR"
    else
        log "Docker image $img already exists (context: $ctx)"
    fi
}

sync_source() {
    local host=$1 remote_dir=$2
    log "Syncing source to $host:$remote_dir..."
    rsync -az --delete \
        --exclude='.git' --exclude='build/' --exclude='target/' \
        --exclude='script/enum-generator/venv/' \
        "$BASEDIR/" "$host:$remote_dir/"

    # build.sh needs git rev-parse
    ssh "$host" "cd $remote_dir && \
        if [ ! -d .git ]; then \
            git init && git config user.email build@local && git config user.name Build && \
            git add -A && git commit -m 'build snapshot'; \
        else \
            git add -A && git diff-index --quiet HEAD || git commit -m 'update snapshot'; \
        fi" 2>/dev/null
}

docker_build() {
    local ctx=$1 img=$2 remote_dir=$3 flags=$4
    DOCKER_CONTEXT="$ctx" docker run --rm \
        -v "$remote_dir:/jvips" -w /jvips \
        "$img" bash -l -ex build.sh $flags $SKIP_TEST
}

# --- Preflight checks ---
NEED_X86=$((BUILD_X86))
NEED_ARM=$((BUILD_ARM || BUILD_SVE2))

if [[ $NEED_X86 -eq 1 ]]; then
    log "Checking x86_64 Docker VM..."
    check_docker_ctx "$DOCKER_CTX_X86" "x86_64"
fi

if [[ $NEED_ARM -eq 1 ]]; then
    log "Checking arm64 Docker VM..."
    check_docker_ctx "$DOCKER_CTX_ARM" "arm64"
fi

if [[ $BUILD_MACOS -eq 1 ]]; then
    [[ -d "$JAVA8_HOME" ]] || fail "Java 8 not found. Set JAVA8_HOME or install Amazon Corretto 8."
    pkg-config --exists vips 2>/dev/null || fail "vips not found. Install with: brew install vips"
fi

# --- Build Docker images (parallel) ---
PIDS=()

if [[ $NEED_X86 -eq 1 ]]; then
    ensure_docker_image "$DOCKER_CTX_X86" "$DOCKER_IMG_X86" &
    PIDS+=($!)
fi

if [[ $NEED_ARM -eq 1 ]]; then
    ensure_docker_image "$DOCKER_CTX_ARM" "$DOCKER_IMG_ARM" &
    PIDS+=($!)
fi

for pid in "${PIDS[@]}"; do wait "$pid" || fail "Docker image build failed"; done
PIDS=()

# --- Sync source (parallel) ---
if [[ $NEED_X86 -eq 1 ]]; then
    sync_source "$SSH_X86" "$REMOTE_DIR_X86" &
    PIDS+=($!)
fi

if [[ $NEED_ARM -eq 1 ]]; then
    sync_source "$SSH_ARM" "$REMOTE_DIR_ARM" &
    PIDS+=($!)
fi

for pid in "${PIDS[@]}"; do wait "$pid" || fail "Source sync failed"; done
PIDS=()

# --- Native builds (parallel) ---
if [[ $BUILD_X86 -eq 1 ]]; then
    log "Building linux-x86_64..."
    docker_build "$DOCKER_CTX_X86" "$DOCKER_IMG_X86" "$REMOTE_DIR_X86" \
        "--with-linux-x86_64" &
    PIDS+=($!)
fi

if [[ $NEED_ARM -eq 1 ]]; then
    ARM_FLAGS=""
    [[ $BUILD_ARM  -eq 1 ]] && ARM_FLAGS+=" --with-linux-aarch64"
    [[ $BUILD_SVE2 -eq 1 ]] && ARM_FLAGS+=" --with-linux-aarch64-sve2"
    log "Building${ARM_FLAGS}..."
    docker_build "$DOCKER_CTX_ARM" "$DOCKER_IMG_ARM" "$REMOTE_DIR_ARM" \
        "$ARM_FLAGS" &
    PIDS+=($!)
fi

if [[ $BUILD_MACOS -eq 1 ]]; then
    log "Building darwin-aarch64..."
    (
        rm -rf "$BASEDIR/build/macOS"
        export JAVA_HOME="$JAVA8_HOME"
        export PATH="$JAVA_HOME/bin:$PATH"
        bash -l -ex "$BASEDIR/build.sh" --with-macos $SKIP_TEST
    ) &
    PIDS+=($!)
fi

for pid in "${PIDS[@]}"; do wait "$pid" || fail "Native build failed (pid $pid)"; done
PIDS=()

# --- Collect artifacts ---
log "Collecting build artifacts..."

if [[ $BUILD_X86 -eq 1 ]]; then
    rsync -az "$SSH_X86:$REMOTE_DIR_X86/build/linux-x86_64/" "$BASEDIR/build/linux-x86_64/"
fi

if [[ $BUILD_ARM -eq 1 ]]; then
    rsync -az "$SSH_ARM:$REMOTE_DIR_ARM/build/linux-aarch64/" "$BASEDIR/build/linux-aarch64/"
fi

if [[ $BUILD_SVE2 -eq 1 ]]; then
    rsync -az "$SSH_ARM:$REMOTE_DIR_ARM/build/linux-aarch64-sve2/" "$BASEDIR/build/linux-aarch64-sve2/"
fi

# darwin-aarch64 is already local

# --- Package JAR ---
log "Packaging JVips.jar..."
export JAVA_HOME="$JAVA8_HOME"
export PATH="$JAVA_HOME/bin:$PATH"

GIT_SHORT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
VERSION="8.18.0-${GIT_SHORT}"

mvn --batch-mode \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    -DnewVersion="$VERSION" versions:set

mvn --batch-mode \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    -DskipTests clean package

mvn --batch-mode versions:revert

# --- Verify ---
log "Verifying JAR contents..."
echo ""
for dir in linux-x86_64 linux-aarch64 linux-aarch64-sve2 darwin-aarch64; do
    count=$(jar tf JVips.jar | grep "^${dir}/" | grep -v '/$' | wc -l | tr -d ' ')
    if [[ $count -gt 0 ]]; then
        printf "  %-25s %s libs\n" "$dir" "$count"
    else
        printf "  %-25s MISSING\n" "$dir"
    fi
done
echo ""

JAR_SIZE=$(ls -lh JVips.jar | awk '{print $5}')
log "Done! JVips.jar ($JAR_SIZE) at: $BASEDIR/JVips.jar"
