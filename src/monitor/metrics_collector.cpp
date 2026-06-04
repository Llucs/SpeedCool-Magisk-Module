#include "monitor/metrics_collector.h"
#include "common/logging.h"
#include "pal/cpu/cpu_info.h"
#include "pal/cpu/cpu_freq.h"
#include "pal/memory/ram_info.h"
#include "pal/thermal/thermal.h"
#include "pal/gpu/gpu_info.h"
#include "pal/power/power.h"
#include <thread>
#include <chrono>

namespace speedcool::monitor {

MetricsCollector::MetricsCollector() {
    prev_cpu_ = read_cpu_stats();
}

auto MetricsCollector::collect() -> Metrics {
    Metrics m{};
    m.timestamp = std::chrono::system_clock::now();

    auto curr_cpu = read_cpu_stats();
    if (!first_sample_) {
        m.cpu_usage = calculate_cpu_usage(prev_cpu_, curr_cpu);
        m.io_wait = calculate_io_wait(prev_cpu_, curr_cpu);
    } else {
        first_sample_ = false;
    }
    prev_cpu_ = curr_cpu;

    auto la = read_load_avg();
    m.load_1m = la.load_1m;
    m.load_5m = la.load_5m;
    m.load_15m = la.load_15m;

    auto mem = pal::get_memory_info();
    m.ram_percent = mem.usage_percent;

    auto power = pal::get_power_info();
    m.battery_level = power.battery_level;
    m.on_ac = power.on_ac;

    m.cpu_freqs = pal::get_current_frequencies();

    pal::ThermalController therm;
    m.cpu_temp = therm.cpu_temp_celsius();

    pal::GpuController gpu;
    m.gpu_temp = gpu.temperature_celsius();
    m.gpu_active = gpu.is_active();

    auto now = std::chrono::system_clock::to_time_t(m.timestamp);
    std::tm tm{};
#ifdef _WIN32
    localtime_s(&tm, &now);
#else
    localtime_r(&now, &tm);
#endif
    m.hour = tm.tm_hour;

    return m;
}

auto MetricsCollector::start_background_collection() -> void {
    log_info("Background metrics collection started");
}

auto MetricsCollector::stop_background_collection() -> void {
    log_info("Background metrics collection stopped");
}

} // namespace speedcool::monitor
