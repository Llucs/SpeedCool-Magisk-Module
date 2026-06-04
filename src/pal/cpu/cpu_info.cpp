#include "pal/cpu/cpu_info.h"
#include "common/logging.h"
#include <fstream>
#include <sstream>
#include <filesystem>
#include <unordered_set>

namespace fs = std::filesystem;

namespace speedcool::pal {

static auto read_sysfs_str(const std::string& path) -> std::string {
    std::ifstream ifs(path);
    if (!ifs) return {};
    std::string val;
    ifs >> val;
    return val;
}

static auto read_sysfs_u32(const std::string& path) -> u32 {
    auto s = read_sysfs_str(path);
    if (s.empty()) return 0;
    try { return std::stoul(s); } catch (...) { return 0; }
}

auto detect_cpu_topology() -> CpuTopology {
    CpuTopology topo;
    topo.arch = read_sysfs_str("/proc/sys/kernel/arch");
    if (topo.arch.empty()) topo.arch = "unknown";

    std::unordered_set<u32> core_ids;
    std::unordered_set<u32> socket_ids;

    for (u32 cpu = 0; ; ++cpu) {
        auto path = std::format("/sys/devices/system/cpu/cpu{}/topology/core_id", cpu);
        if (!fs::exists(path)) break;

        auto core_id = read_sysfs_u32(path);
        auto socket_id = read_sysfs_u32(
            std::format("/sys/devices/system/cpu/cpu{}/topology/physical_package_id", cpu));
        auto freq = read_sysfs_u32(
            std::format("/sys/devices/system/cpu/cpu{}/cpufreq/scaling_cur_freq", cpu)) / 1000;
        auto gov = read_sysfs_str(
            std::format("/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor", cpu));

        core_ids.insert(core_id);
        socket_ids.insert(socket_id);
        topo.core_frequencies_mhz.push_back(freq ? freq : 0);
        topo.governors.push_back(gov.empty() ? "unknown" : gov);

        auto type_path = std::format("/sys/devices/system/cpu/cpu{}/topology/core_type", cpu);
        auto type = read_sysfs_str(type_path);
        if (type == "small" || type == "efficiency") topo.hybrid = true;
    }

    topo.logical_cores = static_cast<u32>(topo.core_frequencies_mhz.size());
    topo.physical_cores = static_cast<u32>(core_ids.size());
    topo.sockets = static_cast<u32>(socket_ids.size());

    log_info("CPU: {} logical, {} physical, {} sockets, hybrid={}",
             topo.logical_cores, topo.physical_cores, topo.sockets, topo.hybrid);

    return topo;
}

} // namespace speedcool::pal
