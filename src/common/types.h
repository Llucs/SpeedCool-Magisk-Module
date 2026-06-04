#pragma once

#include <cstdint>
#include <string>
#include <vector>
#include <chrono>

namespace speedcool {

using i8  = std::int8_t;
using i16 = std::int16_t;
using i32 = std::int32_t;
using i64 = std::int64_t;
using u8  = std::uint8_t;
using u16 = std::uint16_t;
using u32 = std::uint32_t;
using u64 = std::uint64_t;
using f32 = float;
using f64 = double;

using MHz = u32;
using KB  = u64;
using MB  = u64;
using GB  = u64;
using Celsius = f64;
using Percent = f64;

struct CpuTopology {
    u32 physical_cores{};
    u32 logical_cores{};
    u32 sockets{};
    std::vector<u32> core_frequencies_mhz;
    std::vector<std::string> governors;
    std::string arch;
    bool hybrid{};
};

struct MemoryInfo {
    GB total_ram{};
    GB available_ram{};
    GB used_ram{};
    GB total_swap{};
    GB used_swap{};
    u64 page_size{};
    Percent usage_percent{};
};

struct BlkDevice {
    std::string name;
    std::string scheduler;
    bool rotational{};
    bool nvme{};
    KB read_ahead_kb{};
};

struct ThermalZone {
    std::string name;
    Celsius temp{};
    std::string type;
};

struct PowerInfo {
    Percent battery_level{};
    bool on_ac{};
    std::string power_scheme;
};

struct Metrics {
    f64 cpu_usage{};
    f64 cpu_temp{};
    std::vector<MHz> cpu_freqs;
    Percent ram_percent{};
    Percent swap_percent{};
    f64 io_wait{};
    f64 load_1m{};
    f64 load_5m{};
    f64 load_15m{};
    i32 hour{};
    bool gpu_active{};
    Celsius gpu_temp{};
    std::string foreground_app;
    Percent battery_level{};
    bool on_ac{};
    bool conflito_detectado{};
    std::chrono::system_clock::time_point timestamp;
};

enum class ProfileType : u8 {
    Eco = 0,
    Balanced = 1,
    Performance = 2,
    Gaming = 3,
    Custom = 4
};

struct ProfileConfig {
    ProfileType type{ProfileType::Balanced};
    std::string cpu_governor{"schedutil"};
    u32 cpu_freq_limit_pct{};
    std::string io_scheduler{"bfq"};
    std::string gpu_governor{"simple_ondemand"};
    Percent ram_clean_threshold_pct{85.0};
    u32 swappiness{60};
    std::string power_scheme{"balanced"};
    std::string process_priority{"below_normal"};
    std::string mmcss_task{""};
    bool game_mode{};
    bool disable_core_parking{};
};

struct AdaptiveState {
    f64 learning_rate{0.01};
    bool predictive_mode{true};
    std::array<f64, 4> weights{};
    u64 samples_collected{};
};

struct DaemonConfig {
    u32 interval_sec{300};
    std::string log_level{"info"};
    std::string log_file{"/var/log/speedcool.log"};
    bool adaptive_enabled{true};
    bool telemetry_enabled{false};
    u32 telemetry_retention_days{30};
    bool auto_update_check{true};
    std::string update_channel{"stable"};
    std::string github_repo{"user/speedcool-cpp"};
    std::vector<std::string> known_conflict_modules;
    bool auto_resolve_conflicts{true};
};

} // namespace speedcool
