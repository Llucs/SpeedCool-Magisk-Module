#include "pal/memory/ram_info.h"
#include "common/logging.h"
#include <fstream>
#include <unordered_map>
#include <filesystem>

namespace fs = std::filesystem;

namespace speedcool::pal {

auto get_memory_info() -> MemoryInfo {
    MemoryInfo mem{};

    std::ifstream ifs("/proc/meminfo");
    if (!ifs) {
        log_warn("Cannot read /proc/meminfo");
        return mem;
    }

    std::unordered_map<std::string, u64> fields;
    std::string key;
    u64 value;
    std::string unit;

    while (ifs >> key >> value >> unit) {
        key.pop_back();
        fields[key] = value;
    }

    mem.total_ram    = fields["MemTotal"] / (1024 * 1024);
    mem.available_ram = fields["MemAvailable"] / (1024 * 1024);
    mem.used_ram     = mem.total_ram - mem.available_ram;
    mem.total_swap   = fields["SwapTotal"] / (1024 * 1024);
    mem.used_swap    = fields["SwapCached"] / (1024 * 1024);

    if (mem.total_ram > 0) {
        mem.usage_percent = (static_cast<f64>(mem.used_ram) / mem.total_ram) * 100.0;
    }

    log_debug("RAM: {}GB total, {}GB used ({:.1f}%), {}GB swap",
              mem.total_ram, mem.used_ram, mem.usage_percent, mem.total_swap);

    return mem;
}

auto get_hugepages_info() -> std::string {
    std::string result;
    auto hp_path = fs::path("/sys/kernel/mm/hugepages");
    if (!fs::exists(hp_path)) return {};

    for (auto& entry : fs::directory_iterator(hp_path)) {
        auto size = entry.path().filename().string();
        std::ifstream nr(entry.path() / "nr_hugepages");
        std::ifstream free(entry.path() / "free_hugepages");
        std::string n, f;
        nr >> n; free >> f;
        if (!result.empty()) result += ", ";
        result += std::format("{}: {} allocated, {} free", size, n, f);
    }
    return result;
}

} // namespace speedcool::pal
