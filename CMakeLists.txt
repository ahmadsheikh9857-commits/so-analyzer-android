cmake_minimum_required(VERSION 3.22.1)

project("soanalyzer")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add source files
add_library(soanalyzer SHARED
    disassembler.cpp
)

# Link against Android log library
find_library(log-lib log)
target_link_libraries(soanalyzer ${log-lib})

# TODO: Add Capstone library
# find_package(Capstone REQUIRED)
# target_link_libraries(soanalyzer Capstone::Capstone)

# Include directories
target_include_directories(soanalyzer PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
)

# Compiler flags
if(ANDROID)
    # Android-specific flags
    target_compile_options(soanalyzer PRIVATE
        -Wall
        -Wextra
        -Werror
        -fPIC
    )
endif()
