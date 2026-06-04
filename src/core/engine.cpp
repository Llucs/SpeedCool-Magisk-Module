#include "core/engine.h"
#include "common/logging.h"
#include <algorithm>
#include <cmath>
#include <ranges>
#include <filesystem>
#include <fstream>

namespace speedcool::engine {

auto decide(const Metrics& m, const DaemonConfig& cfg) -> Decision {
    Decision d;

    if (m.conflito_detectado && cfg.auto_resolve_conflicts) {
        d.profile = ProfileType::Balanced;
        d.confidence = 0.5;
        log_info("Conflict detected, reverting to Balanced");
        return d;
    }

    if (m.hour >= 1 && m.hour <= 6) {
        d.profile = ProfileType::Eco;
        d.confidence = 0.8;
        log_debug("Idle hours -> Eco profile");
        return d;
    }

    if (m.gpu_active && m.cpu_usage > 50.0) {
        d.profile = ProfileType::Gaming;
        d.confidence = 0.7 + (m.cpu_usage > 80.0 ? 0.15 : 0.0);
        log_debug("GPU active + high CPU -> Gaming profile");
        return d;
    }

    if (m.cpu_usage > 70.0 || m.ram_percent > 85.0) {
        d.profile = ProfileType::Performance;
        d.confidence = 0.75;
        log_debug("High load -> Performance profile");
        return d;
    }

    if (m.cpu_usage > 35.0 || m.ram_percent > 60.0) {
        d.profile = ProfileType::Balanced;
        d.confidence = 0.6;
        return d;
    }

    d.profile = ProfileType::Eco;
    d.confidence = 0.9;
    log_debug("Low load -> Eco profile");
    return d;
}

auto calculate_next_interval(const Metrics& m, u32 base_interval_sec) -> u32 {
    u32 interval = base_interval_sec;

    if (m.cpu_temp > 85.0) {
        interval = std::min(interval, 60u);
    } else if (m.cpu_temp > 75.0) {
        interval = std::min(interval, 120u);
    }

    if (m.cpu_usage > 90.0 || m.ram_percent > 95.0) {
        interval = std::min(interval, 30u);
    }

    return interval;
}

auto check_conflicts(const std::vector<std::string>& known_modules) -> bool {
    for (const auto& mod : known_modules) {
        auto path = std::format("/proc/sys/kernel/{}", mod);
        if (std::filesystem::exists(path)) {
            log_warn("Conflict detected: {}", mod);
            return true;
        }
    }

    for (const auto& entry : std::filesystem::directory_iterator("/proc")) {
        auto name = entry.path().filename().string();
        try {
            u32 pid = std::stoul(name);
            std::ifstream comm(entry.path() / "comm");
            std::string cmd;
            comm >> cmd;

            auto it = std::ranges::find_if(known_modules, [&](const std::string& mod) {
                return cmd.find(mod) != std::string::npos;
            });
            if (it != known_modules.end()) {
                log_warn("Conflict: process {} ({}) matches known module", pid, cmd);
                return true;
            }
        } catch (...) {}
    }

    return false;
}

Predictor::Predictor(f64 learning_rate)
    : learning_rate_(learning_rate)
{
    weights_[0] = 0.25; // cpu_usage
    weights_[1] = 0.25; // ram_percent
    weights_[2] = 0.25; // cpu_temp
    weights_[3] = 0.25; // battery_level
}

auto Predictor::predict(const Metrics& m) -> f64 {
    return weights_[0] * (m.cpu_usage / 100.0)
         + weights_[1] * (m.ram_percent / 100.0)
         + weights_[2] * (m.cpu_temp / 100.0)
         + weights_[3] * (m.battery_level / 100.0);
}

auto Predictor::train(const Metrics& m, f64 actual_score) -> void {
    f64 error = actual_score - predict(m);
    weights_[0] += learning_rate_ * error * (m.cpu_usage / 100.0);
    weights_[1] += learning_rate_ * error * (m.ram_percent / 100.0);
    weights_[2] += learning_rate_ * error * (m.cpu_temp / 100.0);
    weights_[3] += learning_rate_ * error * (m.battery_level / 100.0);

    f64 norm = std::sqrt(weights_[0]*weights_[0] + weights_[1]*weights_[1] +
                          weights_[2]*weights_[2] + weights_[3]*weights_[3]);
    if (norm > 0.0) {
        for (auto& w : weights_) w /= norm;
    }
}

auto Predictor::weights() const -> const std::array<f64, 4>& {
    return weights_;
}

auto Predictor::set_weights(const std::array<f64, 4>& w) -> void {
    weights_ = w;
}

} // namespace speedcool::engine
