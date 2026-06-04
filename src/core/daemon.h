#pragma once

#include "common/types.h"
#include "common/error.h"
#include "core/config.h"
#include "core/scheduler.h"
#include "core/profile_manager.h"
#include "core/engine.h"
#include "monitor/metrics_collector.h"
#include <memory>
#include <atomic>

namespace speedcool::core {

class Daemon {
public:
    Daemon();
    ~Daemon();

    auto initialize(const std::string& config_path) -> Result<void>;
    auto run() -> void;
    auto stop() -> void;
    auto is_running() const -> bool;
    auto status() -> std::string;
    auto force_profile(ProfileType type) -> Result<void>;
    auto set_adaptive(bool enabled) -> void;

private:
    config::FullConfig cfg_;
    std::unique_ptr<profile::ProfileManager> profile_mgr_;
    std::unique_ptr<monitor::MetricsCollector> collector_;
    std::unique_ptr<Scheduler> scheduler_;
    engine::Predictor predictor_;
    std::atomic<bool> running_{false};
    std::atomic<bool> adaptive_{true};

    auto engine_cycle() -> void;
    auto check_platform() -> Result<void>;
};

} // namespace speedcool::core
