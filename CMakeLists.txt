cmake_minimum_required(VERSION 4.0)
project(speedcool VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 26)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# --- Dependencies ---
find_package(PkgConfig QUIET)

function(find_pkg_config VAR name)
    if(PkgConfig_FOUND)
        pkg_check_modules(${VAR} ${name})
    endif()
    if(${VAR}_FOUND)
        set(${VAR}_INCLUDE_DIRS ${${VAR}_INCLUDE_DIRS} PARENT_SCOPE)
        set(${VAR}_LIBRARIES ${${VAR}_LIBRARIES} PARENT_SCOPE)
        set(${VAR}_FOUND TRUE PARENT_SCOPE)
        return()
    endif()
endfunction()

find_pkg_config(SPDLOG spdlog)
if(NOT SPDLOG_FOUND)
    find_path(SPDLOG_INCLUDE_DIRS spdlog/spdlog.h)
    if(SPDLOG_INCLUDE_DIRS)
        set(SPDLOG_FOUND TRUE)
        set(SPDLOG_LIBRARIES spdlog)
        message(STATUS "Found spdlog via find_path")
    endif()
endif()

find_pkg_config(FMT fmt)
if(NOT FMT_FOUND)
    find_path(FMT_INCLUDE_DIRS fmt/core.h)
    if(FMT_INCLUDE_DIRS)
        set(FMT_FOUND TRUE)
        set(FMT_LIBRARIES fmt)
        message(STATUS "Found fmt via find_path")
    endif()
endif()

find_pkg_config(FTXUI ftxui)
if(NOT FTXUI_FOUND)
    find_path(FTXUI_INCLUDE_DIRS ftxui/dom/elements.hpp)
    find_library(FTXUI_LIBRARIES ftxui-dom)
    if(FTXUI_INCLUDE_DIRS AND FTXUI_LIBRARIES)
        set(FTXUI_FOUND TRUE)
        message(STATUS "Found ftxui via find_path")
    endif()
endif()

find_path(TOML11_INCLUDE_DIRS toml.hpp)

if(NOT SPDLOG_FOUND)
    message(FATAL_ERROR "spdlog is required. Install libspdlog-dev")
endif()
if(NOT FMT_FOUND)
    message(FATAL_ERROR "fmt is required. Install libfmt-dev")
endif()

include(CheckCXXCompilerFlag)
check_cxx_compiler_flag(-fcontracts HAS_CONTRACTS)
if(HAS_CONTRACTS)
    add_compile_options(-fcontracts)
endif()

add_compile_options(-Wall -Wextra -Wpedantic -Werror -DSPEEDCOOL_VERSION="${PROJECT_VERSION}")
include_directories(${TOML11_INCLUDE_DIRS} ${SPDLOG_INCLUDE_DIRS} ${FMT_INCLUDE_DIRS} ${FTXUI_INCLUDE_DIRS})

add_library(speedcool_core STATIC
    src/common/logging.cpp
    src/common/types.h
    src/common/error.h
    src/core/config.cpp
    src/core/engine.cpp
    src/core/profile_manager.cpp
    src/core/scheduler.cpp
    src/pal/cpu/cpu_info.cpp
    src/pal/cpu/cpu_freq.cpp
    src/pal/cpu/cpu_gov.cpp
    src/pal/memory/ram_info.cpp
    src/pal/memory/vm_tuning.cpp
    src/pal/io/io_scheduler.cpp

    src/pal/thermal/thermal.cpp
    src/pal/gpu/gpu_info.cpp
    src/pal/power/power.cpp
    src/pal/platform_linux.cpp
    src/monitor/metrics_collector.cpp
    src/monitor/proc_stats.cpp
    src/update/updater.cpp
    src/update/manifest.cpp
)

target_include_directories(speedcool_core PUBLIC src)
target_link_libraries(speedcool_core PUBLIC
    ${SPDLOG_LIBRARIES}
    ${FMT_LIBRARIES}
)

add_executable(speedcool
    src/main.cpp
    src/core/daemon.cpp
    src/cli/cli.cpp
    src/cli/tui.cpp
)

if(FTXUI_FOUND)
    target_compile_definitions(speedcool PRIVATE SPEEDCOOL_HAS_TUI=1)
endif()

target_link_libraries(speedcool PRIVATE speedcool_core
    ${SPDLOG_LIBRARIES}
    ${FMT_LIBRARIES}
    ${FTXUI_LIBRARIES}
)

if(WIN32)
    target_sources(speedcool_core PRIVATE src/pal/platform_windows.cpp)
    target_link_libraries(speedcool_core PRIVATE advapi32 powrprof pdh)
endif()

function(add_speedcool_test name)
    add_executable(${name} ${ARGN})
    target_link_libraries(${name} PRIVATE speedcool_core
        ${SPDLOG_LIBRARIES}
        ${FMT_LIBRARIES}
    )
    target_compile_options(${name} PRIVATE -DSCTEST)
endfunction()

add_speedcool_test(test_cpu tests/unit/test_cpu.cpp)
add_speedcool_test(test_memory tests/unit/test_memory.cpp)
add_speedcool_test(test_config tests/unit/test_config.cpp)
add_speedcool_test(test_engine tests/unit/test_engine.cpp)
add_speedcool_test(test_profile tests/unit/test_profile_manager.cpp)
add_speedcool_test(test_daemon_integration tests/integration/test_daemon_integration.cpp src/core/daemon.cpp)

install(TARGETS speedcool RUNTIME DESTINATION bin)
install(FILES config/speedcool.toml DESTINATION /etc/speedcool)
