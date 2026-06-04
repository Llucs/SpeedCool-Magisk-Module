cmake_minimum_required(VERSION 3.22)
project(speedcool VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

add_compile_options(-Wall -Wextra -Wpedantic -Werror -DSPEEDCOOL_VERSION="${PROJECT_VERSION}")

# Try C++26 if compiler supports it
include(CheckCXXCompilerFlag)
check_cxx_compiler_flag("-std=c++26" CXX26_OK)
if(CXX26_OK)
    set(CMAKE_CXX_STANDARD 26)
    message(STATUS "Using C++26")
endif()

check_cxx_compiler_flag("-fcontracts" HAS_CONTRACTS)
if(HAS_CONTRACTS)
    add_compile_options(-fcontracts)
endif()

# Dependencies: prefer pkg-config, fallback to find_path/find_library
find_package(PkgConfig QUIET)

macro(find_dep VAR header lib)
    if(PkgConfig_FOUND)
        pkg_check_modules(PC_${VAR} ${VAR} QUIET)
    endif()
    if(PC_${VAR}_FOUND)
        set(${VAR}_INCLUDE_DIRS ${PC_${VAR}_INCLUDE_DIRS})
        set(${VAR}_LIBRARIES ${PC_${VAR}_LIBRARIES})
        message(STATUS "Found ${VAR} via pkg-config")
    else()
        find_path(${VAR}_INCLUDE_DIR ${header})
        find_library(${VAR}_LIBRARY ${lib})
        if(${VAR}_INCLUDE_DIR)
            set(${VAR}_INCLUDE_DIRS ${${VAR}_INCLUDE_DIR})
            if(${VAR}_LIBRARY)
                set(${VAR}_LIBRARIES ${${VAR}_LIBRARY})
            endif()
            message(STATUS "Found ${VAR} via find_path: ${${VAR}_INCLUDE_DIR}")
        else()
            message(FATAL_ERROR "Required dependency ${VAR} not found (${header})")
        endif()
    endif()
endmacro()

find_dep(spdlog spdlog/spdlog.h spdlog)
find_dep(fmt fmt/core.h fmt)
find_dep(toml11 toml.hpp "")

# FTXUI is optional
find_path(FTXUI_INCLUDE_DIR ftxui/dom/elements.hpp)
find_library(FTXUI_LIB ftxui-dom)
if(FTXUI_INCLUDE_DIR AND FTXUI_LIB)
    set(FTXUI_FOUND TRUE)
    set(FTXUI_INCLUDE_DIRS ${FTXUI_INCLUDE_DIR})
    set(FTXUI_LIBRARIES ${FTXUI_LIB})
    add_compile_definitions(SPEEDCOOL_HAS_TUI=1)
    message(STATUS "Found ftxui")
else()
    message(STATUS "ftxui not found, TUI disabled")
endif()

# nlohmann-json is optional (for telemetry)
find_path(NLOHMANN_INCLUDE_DIR nlohmann/json.hpp)
if(NLOHMANN_INCLUDE_DIR)
    add_compile_definitions(SPEEDCOOL_HAS_JSON=1)
    message(STATUS "Found nlohmann-json")
endif()

# Core library
add_library(speedcool_core STATIC
    src/common/logging.cpp
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

target_include_directories(speedcool_core PUBLIC src
    ${spdlog_INCLUDE_DIRS}
    ${fmt_INCLUDE_DIRS}
    ${toml11_INCLUDE_DIRS}
    ${FTXUI_INCLUDE_DIRS}
    ${NLOHMANN_INCLUDE_DIR}
)

target_link_libraries(speedcool_core PUBLIC
    ${spdlog_LIBRARIES}
    ${fmt_LIBRARIES}
)

if(WIN32)
    target_sources(speedcool_core PRIVATE src/pal/platform_windows.cpp)
    target_link_libraries(speedcool_core PRIVATE advapi32 powrprof pdh)
endif()

# Main binary
add_executable(speedcool
    src/main.cpp
    src/core/daemon.cpp
    src/cli/cli.cpp
    src/cli/tui.cpp
)

target_link_libraries(speedcool PRIVATE speedcool_core ${FTXUI_LIBRARIES})

# Tests
function(add_speedcool_test name)
    add_executable(${name} ${ARGN})
    target_link_libraries(${name} PRIVATE speedcool_core)
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
