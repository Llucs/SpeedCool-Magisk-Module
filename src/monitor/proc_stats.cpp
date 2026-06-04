#include "monitor/proc_stats.h"
#include <fstream>
#include <sstream>
#include <algorithm>

namespace speedcool::monitor {

auto read_cpu_stats() -> CpuStats {
    CpuStats stats{};
    std::ifstream ifs("/proc/stat");
    if (!ifs) return stats;

    std::string line;
    std::getline(ifs, line);
    std::istringstream iss(line);
    std::string cpu;
    iss >> cpu >> stats.user >> stats.nice >> stats.system >> stats.idle
        >> stats.iowait >> stats.irq >> stats.softirq >> stats.steal;
    return stats;
}

auto read_load_avg() -> LoadAvg {
    LoadAvg la{};
    std::ifstream ifs("/proc/loadavg");
    if (!ifs) return la;
    ifs >> la.load_1m >> la.load_5m >> la.load_15m;
    return la;
}

auto calculate_cpu_usage(const CpuStats& prev, const CpuStats& curr) -> f64 {
    auto prev_idle = prev.idle + prev.iowait;
    auto curr_idle = curr.idle + curr.iowait;

    auto prev_total = prev.user + prev.nice + prev.system + prev.idle +
                      prev.iowait + prev.irq + prev.softirq + prev.steal;
    auto curr_total = curr.user + curr.nice + curr.system + curr.idle +
                      curr.iowait + curr.irq + curr.softirq + curr.steal;

    auto total_diff = curr_total - prev_total;
    auto idle_diff = curr_idle - prev_idle;

    if (total_diff == 0) return 0.0;
    return 100.0 * (1.0 - static_cast<f64>(idle_diff) / total_diff);
}

auto calculate_io_wait(const CpuStats& prev, const CpuStats& curr) -> f64 {
    auto prev_total = prev.user + prev.nice + prev.system + prev.idle +
                      prev.iowait + prev.irq + prev.softirq + prev.steal;
    auto curr_total = curr.user + curr.nice + curr.system + curr.idle +
                      curr.iowait + curr.irq + curr.softirq + curr.steal;

    auto total_diff = curr_total - prev_total;
    auto iowait_diff = curr.iowait - prev.iowait;

    if (total_diff == 0) return 0.0;
    return 100.0 * static_cast<f64>(iowait_diff) / total_diff;
}

} // namespace speedcool::monitor
