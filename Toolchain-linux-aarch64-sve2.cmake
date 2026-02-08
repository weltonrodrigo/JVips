set(BUILD_TARGET linux-aarch64-sve2)

# the name of the target operating system
SET(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_PROCESSOR aarch64)

# which compilers to use for C and C++
SET(CMAKE_C_COMPILER gcc)
SET(CMAKE_CXX_COMPILER g++)
SET(CMAKE_AR ar)
SET(CMAKE_RANLIB ranlib)

# SVE2 architecture flags for Azure Cobalt 100 / Neoverse N2
# These propagate to CMake-based ExternalProject subbuilds (libhwy, libjpeg-turbo, libaom, etc.)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -march=armv8.5-a+sve2" CACHE STRING "" FORCE)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -march=armv8.5-a+sve2" CACHE STRING "" FORCE)

# target environment location
SET(CMAKE_FIND_ROOT_PATH /usr/ ${CMAKE_SOURCE_DIR}/build/${BUILD_TARGET}/inst)

# adjust the default behaviour of the FIND_XXX() commands:
# search headers and libraries in the target environment, search
# programs in the host environment
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)

set(JAVA_INCLUDE_PLATFORM_PATH linux)
