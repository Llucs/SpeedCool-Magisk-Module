#include "pal/cpu/cpu_gov.h"
#include "common/logging.h"
#include <fstream>
#include <filesystem>
#include <sstream>

namespace fs = std::filesystem;

namespace speedcool::pal {

auto get_current_governors() -> std::vector<std::string> {
    std::vector<std::string> govs;
    for (u32 cpu = 0; ; ++cpu) {
        auto path = std::format("/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor", cpu);
        if (!fs::exists(path)) break;
        std::ifstream ifs(path);
        std::string g;
        ifs >> g;
        govs.push_back(g);
    }
    return govs;
}

auto set_all_governors(const std::string& gov) -> bool {
    bool ok = true;
    for (u32 cpu = 0; ; ++cpu) {
        auto path = std::format("/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor", cpu);
        if (!fs::exists(path)) break;
        std::ofstream ofs(path);
        if (!ofs) { ok = false; continue; }
        ofs << gov;
    }
    return ok;
}

auto get_governor_stats(const std::string& gov) -> std::string {
    auto path = std::format("/sys/devices/system/cpu/cpufreq/{}", gov);
    if (!fs::exists(path)) return {};
    std::string result;
    for (auto& entry : fs::directory_iterator(path)) {
        auto fname = entry.path().filename().string();
        std::ifstream ifs(entry.path());
        std::string val;
        ifs >> val;
        if (!result.empty()) result += ", ";
        result += std::format("{}={}", fname, val);
    }
    return result;
}

} // namespace speedcool::pal
