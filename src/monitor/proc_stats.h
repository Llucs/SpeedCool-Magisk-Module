#pragma once

#include "common/types.h"

namespace speedcool::monitor {

struct CpuStats {
    u64 user{};
    u64 nice{};
    u64 system{};
    u64 idle{};
    u64 iowait{};
    u64 irq{};
    u64 softirq{};
    u64 steal{};
};

struct LoadAvg {
    f64 load_1m{};
    f64 load_5m{};
    f64 load_15m{};
};

auto read_cpu_stats() -> CpuStats;
auto read_load_avg() -> LoadAvg;
auto calculate_cpu_usage(const CpuStats& prev, const CpuStats& curr) -> f64;
auto calculate_io_wait(const CpuStats& prev, const CpuStats& curr) -> f64;

} // namespace speedcool::monitor
