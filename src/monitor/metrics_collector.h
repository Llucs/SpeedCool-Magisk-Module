#pragma once

#include "common/types.h"
#include "monitor/proc_stats.h"

namespace speedcool::monitor {

class MetricsCollector {
public:
    MetricsCollector();

    auto collect() -> Metrics;
    auto start_background_collection() -> void;
    auto stop_background_collection() -> void;

private:
    CpuStats prev_cpu_{};
    bool first_sample_{true};
};

} // namespace speedcool::monitor
