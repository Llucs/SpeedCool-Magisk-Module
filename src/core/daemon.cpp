#include "core/daemon.h"
#include "common/logging.h"
#include "pal/cpu/cpu_info.h"
#include "pal/cpu/cpu_freq.h"
#include "pal/memory/ram_info.h"
#include "pal/thermal/thermal.h"
#include "pal/platform.h"

#include <print>

namespace speedcool::core {

Daemon::Daemon()
    : predictor_(0.01) {}

Daemon::~Daemon() {
    stop();
}

auto Daemon::check_platform() -> Result<void> {
    log_info("SpeedCool C++26 v{}", SPEEDCOOL_VERSION);
    log_info("Platform: {} ({})", pal::get_os_info(),
#ifdef __linux__
             "Linux"
#elif _WIN32
             "Windows"
#else
             "Unknown"
#endif
    );

    auto topo = pal::detect_cpu_topology();
    log_info("CPU: {} cores ({} logical), {} sockets, hybrid={}",
             topo.physical_cores, topo.logical_cores, topo.sockets, topo.hybrid);

    auto mem = pal::get_memory_info();
    log_info("RAM: {}GB total, {}GB available", mem.total_ram, mem.available_ram);

    pal::ThermalController therm;
    auto temp = therm.cpu_temp_celsius();
    log_info("CPU temp: {:.1f}°C", temp);

    return {};
}

auto Daemon::initialize(const std::string& config_path) -> Result<void> {
    auto cfg_result = config::load(config_path);
    if (!cfg_result) {
        return std::unexpected(cfg_result.error());
    }
    cfg_ = *cfg_result;

    set_log_level(log_level_from_string(cfg_.daemon.log_level));
    set_log_file(cfg_.daemon.log_file);

    std::println("SpeedCool C++26 Daemon v{}", SPEEDCOOL_VERSION);
    log_info("Initializing...");

    auto platform_check = check_platform();
    if (!platform_check) return platform_check;

    profile_mgr_ = std::make_unique<profile::ProfileManager>(cfg_.profiles);
    collector_ = std::make_unique<monitor::MetricsCollector>();
    predictor_.set_weights(cfg_.adaptive.weights);

    log_info("Initialization complete");
    return {};
}

auto Daemon::engine_cycle() -> void {
    auto metrics = collector_->collect();

    if (engine::check_conflicts(cfg_.known_conflict_modules)) {
        metrics.conflito_detectado = true;
    }

    if (adaptive_) {
        auto decision = engine::decide(metrics, cfg_.daemon);
        auto result = profile_mgr_->apply(decision.profile);
        if (!result) {
            log_error("Failed to apply profile: {}", result.error().message());
        }
    }

    auto next = engine::calculate_next_interval(metrics, cfg_.daemon.interval_sec);
    scheduler_->set_interval(next);
}

auto Daemon::run() -> void {
    if (running_.exchange(true)) {
        log_warn("Daemon already running");
        return;
    }

    log_info("Daemon started (interval: {}s, adaptive: {})",
             cfg_.daemon.interval_sec, adaptive_.load());

    scheduler_ = std::make_unique<Scheduler>(cfg_.daemon.interval_sec);
    scheduler_->start([this]() { engine_cycle(); });
}

auto Daemon::stop() -> void {
    if (!running_.exchange(false)) return;
    if (scheduler_) scheduler_->stop();
    log_info("Daemon stopped");
}

auto Daemon::is_running() const -> bool {
    return running_.load();
}

auto Daemon::status() -> std::string {
    auto mem = pal::get_memory_info();
    pal::ThermalController therm;
    auto temp = therm.cpu_temp_celsius();
    auto freqs = pal::get_current_frequencies();

    std::string s;
    if (running_) {
        s += "Status: RUNNING\n";
    } else {
        s += "Status: STOPPED\n";
    }

    s += std::format("Adaptive: {}\n", adaptive_ ? "ON" : "OFF");
    if (profile_mgr_) {
        s += std::format("Profile: {}\n", profile::ProfileManager::profile_name(profile_mgr_->current()));
    }
    s += std::format("CPU usage: {:.1f}%\n", collector_ ? 0.0 : 0.0);
    s += std::format("RAM: {}MB / {}MB ({:.1f}%)\n", mem.used_ram, mem.total_ram, mem.usage_percent);
    s += std::format("Temp: {:.1f}°C\n", temp);

    if (!freqs.empty()) {
        s += "Frequencies: ";
        for (auto& f : freqs) s += std::format("{}MHz ", f);
    }

    return s;
}

auto Daemon::force_profile(ProfileType type) -> Result<void> {
    if (!profile_mgr_) {
        return std::unexpected(err(Err::ServiceNotRunning, "Profile manager not initialized"));
    }
    auto result = profile_mgr_->apply(type);
    if (result) {
        log_info("Profile forced to {}", profile::ProfileManager::profile_name(type));
    }
    return result;
}

auto Daemon::set_adaptive(bool enabled) -> void {
    adaptive_ = enabled;
    log_info("Adaptive engine {}", enabled ? "enabled" : "disabled");
}

} // namespace speedcool::core
