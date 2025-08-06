# app/src/cpp/CMakeLists.txt
cmake_minimum_required(VERSION 3.22.1)

# Configure project
project("aura-native" LANGUAGES CXX)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Set output directories
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/out)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/out)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/out)

# Add native library
add_library(aura-native SHARED 
    native-lib.cpp
    language_id_l2c_jni.cpp
)

# Find required libraries (Android-specific)
find_library(log-lib log)

# Link libraries
if(log-lib)
    target_link_libraries(aura-native PRIVATE ${log-lib})
else()
    # For non-Android builds, we don't need the log library
    message(STATUS "Android log library not found - building for non-Android target")
endif()

# Set compile options
target_compile_options(aura-native PRIVATE
    -Wall
    -Wextra
    -fexceptions
    -frtti
)

# Set include directories
target_include_directories(aura-native PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
)