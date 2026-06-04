#include "pal/cpu/cpu_freq.h"
#include "common/logging.h"
#include <fstream>
#include <filesystem>
#include <vector>
#include <string>

namespace fs = std::filesystem;

namespace speedcool::pal {

static auto write_sysfs(const std::string& path, const std::string& val) -> bool {
    std::ofstream ofs(path);
    if (!ofs) {
        log_warn("Cannot write {} = {}", path, val);
        return false;
    }
    ofs << val;
    return true;
}

static auto read_sysfs_line(const std::string& path) -> std::string {
    std::ifstream ifs(path);
    if (!ifs) return {};
    std::string line;
    std::getline(ifs, line);
    return line;
}

auto get_current_frequencies() -> std::vector<MHz> {
    std::vector<MHz> freqs;
    for (u32 cpu = 0; ; ++cpu) {
        auto path = std::format("/sys/devices/system/cpu/cpu{}/cpufreq/scaling_cur_freq", cpu);
        if (!fs::exists(path)) break;
        std::ifstream ifs(path);
        if (ifs) {
            MHz val;
            ifs >> val;
            freqs.push_back(val / 1000);
        }
    }
    return freqs;
}

auto get_available_frequencies(u32 cpu) -> std::vector<MHz> {
    auto path = std::format("/sys/devices/system/cpu/cpu{}/cpufreq/scaling_available_frequencies", cpu);
    auto line = read_sysfs_line(path);
    std::vector<MHz> freqs;
    std::istringstream iss(line);
    MHz f;
    while (iss >> f) freqs.push_back(f / 1000);
    return freqs;
}

auto get_available_governors(u32 cpu) -> std::vector<std::string> {
    auto path = std::format("/sys/devices/system/cpu/cpu{}/cpufreq/scaling_available_governors", cpu);
    auto line = read_sysfs_line(path);
    std::vector<std::string> govs;
    std::istringstream iss(line);
    std::string g;
    while (iss >> g) govs.push_back(g);
    return govs;
}

auto set_frequency_limits(u32 cpu, MHz min, MHz max) -> bool {
    auto base = std::format("/sys/devices/system/cpu/cpu{}/cpufreq/", cpu);
    bool ok = true;
    if (min > 0) {
        ok &= write_sysfs(base + "scaling_min_freq", std::to_string(min * 1000));
    }
    if (max > 0) {
        ok &= write_sysfs(base + "scaling_max_freq", std::to_string(max * 1000));
    }
    return ok;
}

auto set_governor(u32 cpu, const std::string& gov) -> bool {
    return write_sysfs(
        std::format("/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor", cpu), gov);
}

auto set_energy_perf_policy(const std::string& policy) -> bool {
    if (fs::exists("/sys/power/energy_perf_policy")) {
        return write_sysfs("/sys/power/energy_perf_policy", policy);
    }
    if (fs::exists("/sys/devices/system/cpu/cpu0/cpufreq/energy_performance_preference")) {
        bool ok = true;
        for (u32 cpu = 0; ; ++cpu) {
            auto path = std::format("/sys/devices/system/cpu/cpu{}/cpufreq/energy_performance_preference", cpu);
            if (!fs::exists(path)) break;
            ok &= write_sysfs(path, policy);
        }
        return ok;
    }
    return false;
}

} // namespace speedcool::pal
