cmake_minimum_required(VERSION 3.15)
project(simplecurl C)

find_package(CURL REQUIRED)

add_executable(${PROJECT_NAME} src/main.c)
target_link_libraries(${PROJECT_NAME}  CURL::libcurl)
