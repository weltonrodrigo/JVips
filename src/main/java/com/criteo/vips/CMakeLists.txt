# Pre-compile java classes for generating JNI header files

set(JAVA_SOURCE_DIRECTORY "${PROJECT_SOURCE_DIR}/src/main/java/com/criteo/vips/")
set(JNI_HEADER_DIRECTORY "${PROJECT_SOURCE_DIR}/src/main/c/")
set(JAVA_SOURCE_FILES
    "${JAVA_SOURCE_DIRECTORY}/Max1Result.java"
    "${JAVA_SOURCE_DIRECTORY}/PixelPacket.java"
    "${JAVA_SOURCE_DIRECTORY}/Vips.java"
    "${JAVA_SOURCE_DIRECTORY}/VipsContext.java"
    "${JAVA_SOURCE_DIRECTORY}/VipsException.java"
    "${JAVA_SOURCE_DIRECTORY}/Image.java"
    "${JAVA_SOURCE_DIRECTORY}/VipsImage.java")
file(GLOB JAVA_ENUM_SOURCE_FILES "${JAVA_SOURCE_DIRECTORY}/enums/*.java")

# Use GENERATE_NATIVE_HEADERS for Java 8+ (replaces deprecated create_javah)
add_jar(JVipsWrapper
    ${JAVA_ENUM_SOURCE_FILES}
    ${JAVA_SOURCE_FILES}
    GENERATE_NATIVE_HEADERS jvips_native_headers
    DESTINATION ${JNI_HEADER_DIRECTORY})

# Create custom target for header dependencies
add_custom_target(Vips_header DEPENDS jvips_native_headers)
add_custom_target(VipsContext_header DEPENDS jvips_native_headers)
add_custom_target(VipsImage_header DEPENDS jvips_native_headers)
add_custom_target(Max1Result_header DEPENDS jvips_native_headers)

if(NOT ${BUILD_TARGET} STREQUAL "macOS")
    add_dependencies(Vips_header libvips)
    add_dependencies(VipsContext_header libvips)
    add_dependencies(VipsImage_header libvips)
    add_dependencies(Max1Result_header libvips)
endif()
