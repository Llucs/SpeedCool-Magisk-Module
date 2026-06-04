#include "pal/power/power.h"
#include "common/logging.h"
#include <fstream>
#include <filesystem>

namespace fs = std::filesystem;

namespace speedcool::pal {

auto get_power_info() -> PowerInfo {
    PowerInfo info{};
    info.battery_level = get_battery_level();
    info.on_ac = is_on_ac();
    return info;
}

auto get_battery_level() -> Percent {
    try {
    for (auto& entry : fs::directory_iterator("/sys/class/power_supply")) {
        auto name = entry.path().filename().string();
        if (name.find("bat") == std::string::npos) continue;

        std::ifstream cap_f(entry.path() / "capacity");
        i32 cap = 0;
        if (cap_f >> cap) return static_cast<Percent>(cap);
    }
    } catch (const fs::filesystem_error&) {}
    return 100.0;
}

auto is_on_ac() -> bool {
    try {
    for (auto& entry : fs::directory_iterator("/sys/class/power_supply")) {
        auto name = entry.path().filename().string();
        if (name.find("AC") == std::string::npos && name.find("ac") == std::string::npos) continue;

        std::ifstream online_f(entry.path() / "online");
        i32 online = 0;
        if (online_f >> online) return online == 1;
    }
    } catch (const fs::filesystem_error&) {}
    return true;
}

auto set_wake_lock(bool enable, const std::string& tag) -> bool {
    if (enable) {
        std::ofstream ofs("/sys/power/wake_lock");
        if (!ofs) return false;
        ofs << tag;
    } else {
        std::ofstream ofs("/sys/power/wake_unlock");
        if (!ofs) return false;
        ofs << tag;
    }
    return true;
}

} // namespace speedcool::pal
