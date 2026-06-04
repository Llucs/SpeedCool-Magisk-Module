cmake_minimum_required(VERSION 3.22)
project(speedcool VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

add_compile_options(-Wall -Wextra -Wpedantic -Werror -DSPEEDCOOL_VERSION="${PROJECT_VERSION}")

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

include(FetchContent)

# === Dependency: spdlog ===
if(MSVC)
    message(STATUS "MSVC: Fetching spdlog via FetchContent")
    set(SPDLOG_BUILD_EXAMPLE OFF CACHE BOOL "" FORCE)
    set(SPDLOG_BUILD_TESTS OFF CACHE BOOL "" FORCE)
    set(SPDLOG_BUILD_BENCH OFF CACHE BOOL "" FORCE)
    FetchContent_Declare(spdlog
        GIT_REPOSITORY https://github.com/gabime/spdlog.git
        GIT_TAG v1.15.1
        GIT_SHALLOW TRUE
    )
    FetchContent_MakeAvailable(spdlog)
    set(SPDLOG_INCLUDE_DIRS ${spdlog_SOURCE_DIR}/include)
    set(SPDLOG_LIBRARIES spdlog::spdlog)
else()
    find_package(spdlog CONFIG QUIET)
    if(spdlog_FOUND)
        set(SPDLOG_LIBRARIES spdlog::spdlog)
        message(STATUS "spdlog via cmake config")
    else()
        find_package(PkgConfig QUIET)
        if(PkgConfig_FOUND)
            pkg_check_modules(PC_spdlog spdlog IMPORTED_TARGET)
        endif()
        if(PC_spdlog_FOUND)
            set(SPDLOG_INCLUDE_DIRS ${PC_spdlog_INCLUDE_DIRS})
            set(SPDLOG_LIBRARIES PkgConfig::PC_spdlog)
            message(STATUS "spdlog via pkg-config")
        else()
            find_path(_spdlog_inc spdlog/spdlog.h)
            find_library(_spdlog_lib spdlog)
            if(_spdlog_inc AND _spdlog_lib)
                set(SPDLOG_INCLUDE_DIRS ${_spdlog_inc})
                set(SPDLOG_LIBRARIES ${_spdlog_lib})
                message(STATUS "spdlog via find_path")
            else()
                message(STATUS "Linux: Fetching spdlog via FetchContent")
                set(SPDLOG_BUILD_EXAMPLE OFF CACHE BOOL "" FORCE)
                set(SPDLOG_BUILD_TESTS OFF CACHE BOOL "" FORCE)
                set(SPDLOG_BUILD_BENCH OFF CACHE BOOL "" FORCE)
                FetchContent_Declare(spdlog
                    GIT_REPOSITORY https://github.com/gabime/spdlog.git
                    GIT_TAG v1.15.1
                    GIT_SHALLOW TRUE
                )
                FetchContent_MakeAvailable(spdlog)
                set(SPDLOG_INCLUDE_DIRS ${spdlog_SOURCE_DIR}/include)
                set(SPDLOG_LIBRARIES spdlog::spdlog)
            endif()
        endif()
    endif()
endif()

# === Dependency: fmt ===
if(MSVC)
    message(STATUS "MSVC: Fetching fmt via FetchContent")
    set(FMT_TEST OFF CACHE BOOL "" FORCE)
    set(FMT_DOC OFF CACHE BOOL "" FORCE)
    FetchContent_Declare(fmt
        GIT_REPOSITORY https://github.com/fmtlib/fmt.git
        GIT_TAG 11.1.4
        GIT_SHALLOW TRUE
    )
    FetchContent_MakeAvailable(fmt)
    set(FMT_INCLUDE_DIRS ${fmt_SOURCE_DIR}/include)
    set(FMT_LIBRARIES fmt::fmt)
else()
    find_package(fmt CONFIG QUIET)
    if(fmt_FOUND)
        set(FMT_LIBRARIES fmt::fmt)
        message(STATUS "fmt via cmake config")
    else()
        if(PkgConfig_FOUND)
            pkg_check_modules(PC_fmt fmt IMPORTED_TARGET)
        endif()
        if(PC_fmt_FOUND)
            set(FMT_INCLUDE_DIRS ${PC_fmt_INCLUDE_DIRS})
            set(FMT_LIBRARIES PkgConfig::PC_fmt)
            message(STATUS "fmt via pkg-config")
        else()
            find_path(_fmt_inc fmt/core.h)
            find_library(_fmt_lib fmt)
            if(_fmt_inc AND _fmt_lib)
                set(FMT_INCLUDE_DIRS ${_fmt_inc})
                set(FMT_LIBRARIES ${_fmt_lib})
                message(STATUS "fmt via find_path")
            else()
                message(STATUS "Linux: Fetching fmt via FetchContent")
                set(FMT_TEST OFF CACHE BOOL "" FORCE)
                set(FMT_DOC OFF CACHE BOOL "" FORCE)
                FetchContent_Declare(fmt
                    GIT_REPOSITORY https://github.com/fmtlib/fmt.git
                    GIT_TAG 11.1.4
                    GIT_SHALLOW TRUE
                )
                FetchContent_MakeAvailable(fmt)
                set(FMT_INCLUDE_DIRS ${fmt_SOURCE_DIR}/include)
                set(FMT_LIBRARIES fmt::fmt)
            endif()
        endif()
    endif()
endif()

# === toml11 (header-only) ===
if(MSVC)
    message(STATUS "MSVC: Downloading toml11 header")
    file(DOWNLOAD https://raw.githubusercontent.com/toruniina/toml11/v3.8.1/toml.hpp
         ${CMAKE_BINARY_DIR}/_deps/toml11/toml.hpp TLS_VERIFY ON)
    set(TOML11_INCLUDE_DIR ${CMAKE_BINARY_DIR}/_deps/toml11)
else()
    find_path(TOML11_INCLUDE_DIR toml.hpp)
    if(NOT TOML11_INCLUDE_DIR)
        message(STATUS "Linux: Downloading toml11 header")
        file(DOWNLOAD https://raw.githubusercontent.com/toruniina/toml11/v3.8.1/toml.hpp
             ${CMAKE_BINARY_DIR}/_deps/toml11/toml.hpp TLS_VERIFY ON)
        set(TOML11_INCLUDE_DIR ${CMAKE_BINARY_DIR}/_deps/toml11)
    endif()
endif()
message(STATUS "toml11: ${TOML11_INCLUDE_DIR}")

# === FTXUI (optional) ===
if(NOT MSVC)
    find_path(FTXUI_INCLUDE_DIR ftxui/dom/elements.hpp)
    find_library(FTXUI_LIB ftxui-dom)
endif()
if(FTXUI_INCLUDE_DIR AND FTXUI_LIB)
    add_compile_definitions(SPEEDCOOL_HAS_TUI=1)
    message(STATUS "Found ftxui: ${FTXUI_INCLUDE_DIR}")
    set(FTXUI_INCLUDE_DIRS ${FTXUI_INCLUDE_DIR})
    set(FTXUI_LIBRARIES ${FTXUI_LIB})
else()
    message(STATUS "ftxui not found, TUI disabled")
endif()

# === nlohmann-json (optional) ===
find_path(NLOHMANN_INCLUDE_DIR nlohmann/json.hpp)
if(NLOHMANN_INCLUDE_DIR)
    add_compile_definitions(SPEEDCOOL_HAS_JSON=1)
    message(STATUS "Found nlohmann-json: ${NLOHMANN_INCLUDE_DIR}")
endif()

# === Core library ===
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
    ${SPDLOG_INCLUDE_DIRS}
    ${FMT_INCLUDE_DIRS}
    ${TOML11_INCLUDE_DIR}
    ${FTXUI_INCLUDE_DIRS}
    ${NLOHMANN_INCLUDE_DIR}
)

target_link_libraries(speedcool_core PUBLIC ${SPDLOG_LIBRARIES} ${FMT_LIBRARIES})

if(WIN32)
    target_sources(speedcool_core PRIVATE src/pal/platform_windows.cpp)
    target_link_libraries(speedcool_core PRIVATE advapi32 powrprof pdh)
endif()

# === Main binary ===
add_executable(speedcool
    src/main.cpp
    src/core/daemon.cpp
    src/cli/cli.cpp
    src/cli/tui.cpp
)

target_link_libraries(speedcool PRIVATE speedcool_core ${FTXUI_LIBRARIES})

# === Tests ===
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
