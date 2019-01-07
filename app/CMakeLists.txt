# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

file(GLOB bzip_source src/main/cpp/bzip/*.c)

add_library(
             native-lib

             SHARED

             src/main/cpp/native-lib.cpp
              src/main/cpp/bspatch.c
              ${bzip_source})

include_directories(src/main/cpp/bzip)

find_library(
              log-lib

              log )

target_link_libraries(
                       native-lib
                       ${log-lib} )