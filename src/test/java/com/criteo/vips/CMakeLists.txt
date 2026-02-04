set(JAVA_TEST_SOURCE_DIRECTORY "${PROJECT_SOURCE_DIR}/src/test/java/com/criteo/vips/")
set(JNI_TEST_HEADER_DIRECTORY "${PROJECT_SOURCE_DIR}/src/test/c/")
set(JAVA_TEST_SOURCE_FILES "${JAVA_TEST_SOURCE_DIRECTORY}/VipsEnumTest.java")
set(JUNIT_HOME "${PROJECT_SOURCE_DIR}/build/artifacts/junit-4.13.1.jar")

# Use GENERATE_NATIVE_HEADERS for Java 8+ (replaces deprecated create_javah)
add_jar(JVipsTestWrapper
    ${JAVA_TEST_SOURCE_FILES}
    "${PROJECT_SOURCE_DIR}/src/main/java/com/criteo/vips//VipsException.java"
    INCLUDE_JARS ${JVipsWrapper} ${JUNIT_HOME}
    GENERATE_NATIVE_HEADERS jvips_test_native_headers
    DESTINATION ${JNI_TEST_HEADER_DIRECTORY})

add_dependencies(JVipsTestWrapper JVipsWrapper)

# Create custom target for header dependency
add_custom_target(VipsEnumTest_header DEPENDS jvips_test_native_headers)
