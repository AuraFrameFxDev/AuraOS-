cmake_minimum_required(VERSION 3.22.1)

# Set project name and version
project(language_id_l2c_jni
        VERSION 1.0.0
        LANGUAGES C CXX
)

# Set C++ standard and properties
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Set common compile flags for all configurations
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -fexceptions -fno-limit-debug-info")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -fexceptions -fno-limit-debug-info")

# Configuration-specific flags
if (CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -DDEBUG=1 -g -O0")
    set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -DDEBUG=1 -g -O0")
else ()
    set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -O2 -DNDEBUG")
    set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O2 -DNDEBUG")
endif ()

# Add source files
add_library(
        ${PROJECT_NAME}
        SHARED
        language_id_l2c_jni.cpp
)

# Find required libraries (conditional for Android)
find_library(log-lib log)

# Set target properties
set_target_properties(${PROJECT_NAME} PROPERTIES
        OUTPUT_NAME "${PROJECT_NAME}"
        CXX_EXTENSIONS OFF
        CXX_STANDARD 17
        CXX_STANDARD_REQUIRED ON
        POSITION_INDEPENDENT_CODE ON
)

# Link libraries (conditional)
if(log-lib)
    target_link_libraries(${PROJECT_NAME} PRIVATE ${log-lib})
    message(STATUS "Android log library found and linked")
else()
    message(STATUS "Android log library not found - building for non-Android target")
endif()

# Set compile options based on build type
if (CMAKE_BUILD_TYPE STREQUAL "Debug")
    target_compile_options(${PROJECT_NAME} PRIVATE
            -O0
            -g
            -fno-omit-frame-pointer
    )
    target_compile_definitions(${PROJECT_NAME} PRIVATE
            -DDEBUG=1
            -D_DEBUG=1
    )
else ()
    target_compile_options(${PROJECT_NAME} PRIVATE
            -O3
            -DNDEBUG
            -fomit-frame-pointer
            -fstrict-aliasing
    )
    target_compile_definitions(${PROJECT_NAME} PRIVATE
            -DNDEBUG=1
    )
endif ()

# Android-specific configurations
if(ANDROID)
    target_compile_definitions(${PROJECT_NAME} PRIVATE
            -DANDROID
            -DANDROID_NDK
    )
    target_link_libraries(${PROJECT_NAME} PRIVATE android)
endif()
