# Migrating jvips to libvips 8.18.0

This document describes the build system changes required to support libvips 8.18.0+.

## Background

libvips 8.18.0 introduced breaking changes for downstream projects:

1. **No more autogen.sh** - The build system switched from autotools to **meson**
2. **No more HTML documentation** - Release tarballs no longer include HTML docs
3. **GIR files are the source of truth** - GObject Introspection (GIR) files now provide enum definitions

The old jvips build:
- Used `./autogen.sh` to configure libvips
- Downloaded HTML docs and parsed them with BeautifulSoup to extract enum values
- Required external Python dependencies (wget, beautifulsoup4)

## Changes Made

### 1. lib/CMakeLists.txt - Meson Build

Replaced the autogen.sh-based ExternalProject_Add with meson:

```cmake
ExternalProject_Add(libvips
  URL "https://github.com/libvips/libvips/archive/v${VIPS_VERSION}.tar.gz"
  PREFIX "${CMAKE_CURRENT_BINARY_DIR}/libvips"
  CONFIGURE_COMMAND ${CMAKE_COMMAND} -E env
    PKG_CONFIG_PATH=${EXT_INSTALL_DIR}/lib/pkgconfig:$ENV{PKG_CONFIG_PATH}
    meson setup
    --prefix=${EXT_INSTALL_DIR}
    --buildtype=release
    --default-library=shared
    -Dintrospection=enabled  # <-- This generates Vips-8.0.gir
    ...
  BUILD_COMMAND meson compile -C ${LIBVIPS_BUILD_DIR}
  INSTALL_COMMAND meson install -C ${LIBVIPS_BUILD_DIR}
  BUILD_IN_SOURCE 0
)
```

Key flag: `-Dintrospection=enabled` generates `Vips-8.0.gir` in `share/gir-1.0/`.

### 2. script/enum-generator/EnumGenerator.py - GIR Parsing

Complete rewrite to parse GIR XML instead of HTML:

- Uses Python's built-in `xml.etree.ElementTree` (no external dependencies)
- Parses `<enumeration>` and `<bitfield>` elements from GIR
- Extracts exact numeric values (no more hardcoded `enum_overwrites` dict)
- Generates Javadoc from `<doc>` elements in GIR

Usage:
```bash
python EnumGenerator.py --gir /path/to/Vips-8.0.gir
```

### 3. Enum Naming Convention

Enum class names retain the `Vips` prefix (e.g., `VipsAccess`, `VipsBandFormat`) for backward compatibility. The enum generator derives Java member names from C identifiers (e.g., `VIPS_FORMAT_UCHAR` → `FormatUchar`).

### 4. build.sh - Build Flow Restructuring

The enum generation now happens **after** libvips is built (so the GIR file exists):

**Linux flow:**
1. Build libvips with meson → generates `${PREFIX}/share/gir-1.0/Vips-8.0.gir`
2. Run EnumGenerator.py with the built GIR file
3. Build JVips JNI wrapper

**macOS flow:**
1. Use homebrew's GIR file at `$(brew --prefix)/share/gir-1.0/Vips-8.0.gir`
2. Run EnumGenerator.py
3. Build JVips JNI wrapper

### 5. Dependencies

**requirements.txt** is now empty (uses Python stdlib only).

**Build environment** needs:
- `meson` and `ninja-build`
- `gobject-introspection` (for GIR generation)
- `glib2` and `glib2-devel`

The Docker image (`.github/docker/linux/Dockerfile`) already has these packages.

## GIR File Structure

The GIR file is XML with this structure:

```xml
<namespace name="Vips" ...>
  <enumeration name="Access" c:type="VipsAccess">
    <doc>How pixels are read from...</doc>
    <member name="random" value="0" c:identifier="VIPS_ACCESS_RANDOM">
      <doc>can read anywhere</doc>
    </member>
    <member name="sequential" value="1" c:identifier="VIPS_ACCESS_SEQUENTIAL">
      <doc>top-to-bottom reading only</doc>
    </member>
  </enumeration>
  <bitfield name="OperationFlags" ...>
    <!-- similar structure -->
  </bitfield>
</namespace>
```

## Testing

1. **Enum value validation**: `VipsEnumTest.c` compares Java enum values against C constants
2. **GIR file check**: Verify `${PREFIX}/share/gir-1.0/Vips-8.0.gir` exists after build

## Migration Phases

### Phase 1: macOS Build (Complete)
- [x] Switch to meson build system
- [x] Implement GIR-based enum generation
- [x] Fix enum naming to retain `Vips` prefix
- [x] Restore `VipsImageFormat.java` (manually maintained)
- [x] Validate macOS build passes all tests

### Phase 2: Linux x86_64 Build (Complete)
- [x] Validate Linux x86_64 Docker build
- [x] Ensure meson build works in Docker environment
- [x] Verify GIR file generation in Linux build
- [x] Fix CentOS 7 EOL mirror issues (switch to vault.centos.org)
- [x] Install cmake3 from EPEL, meson via pip
- [x] Handle lib64 vs lib directory difference on x86_64
- [x] Update libpng to 1.6.43 (old 1.6.37 URL was 404)
- [x] Remove obsolete VipsForeignJpegSubsample enum (renamed to VipsForeignSubsample)

### Phase 3: Linux arm64 Build
- [ ] Add arm64 cross-compilation support
- [ ] Update Docker images for arm64
- [ ] Test on arm64 hardware/emulation

## Known Issues / TODO

- [ ] Windows build may need adjustment (GIR generation untested on MinGW)
- [ ] The meson build doesn't pass all the old autotools options (like `--with-jpeg-includes`) - these are auto-detected via pkg-config

## References

- [libvips meson migration](https://github.com/libvips/libvips/blob/master/meson.build)
- [Homebrew vips formula](https://github.com/Homebrew/homebrew-core/blob/master/Formula/v/vips.rb)
- [GObject Introspection](https://gi.readthedocs.io/)
