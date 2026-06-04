#include "pal/thermal/thermal.h"
#include "common/logging.h"
#include <fstream>
#include <filesystem>
#include <algorithm>

namespace fs = std::filesystem;

namespace speedcool::pal {

auto ThermalController::cpu_temp_celsius() -> Celsius {
    Celsius max_temp = 0;
    try {
    for (auto& entry : fs::directory_iterator("/sys/class/thermal")) {
        auto name = entry.path().filename().string();
        if (!name.starts_with("thermal_zone")) continue;

        std::ifstream type_f(entry.path() / "type");
        std::string type;
        type_f >> type;

        if (type.find("x86_pkg_temp") != std::string::npos ||
            type.find("coretemp") != std::string::npos ||
            type.find("cpu-thermal") != std::string::npos ||
            type.find("k10temp") != std::string::npos) {

            std::ifstream temp_f(entry.path() / "temp");
            i32 raw;
            if (temp_f >> raw) {
                Celsius t = raw / 1000.0;
                max_temp = std::max(max_temp, t);
            }
        }
    }
    } catch (const fs::filesystem_error&) {}
    return max_temp;
}

auto ThermalController::zones() -> std::vector<ThermalZone> {
    std::vector<ThermalZone> result;
    try {
    if (!fs::exists("/sys/class/thermal")) return result;

    for (auto& entry : fs::directory_iterator("/sys/class/thermal")) {
        auto name = entry.path().filename().string();
        if (!name.starts_with("thermal_zone")) continue;

        ThermalZone z;
        z.name = name;

        std::ifstream type_f(entry.path() / "type");
        type_f >> z.type;

        std::ifstream temp_f(entry.path() / "temp");
        i32 raw;
        if (temp_f >> raw) z.temp = raw / 1000.0;

        result.push_back(z);
    }
    } catch (const fs::filesystem_error&) {}
    return result;
}

auto ThermalController::set_cooling_level(unsigned level) -> bool {
    try {
    for (auto& entry : fs::directory_iterator("/sys/class/thermal")) {
        auto name = entry.path().filename().string();
        if (!name.starts_with("cooling_device")) continue;

        auto max_path = entry.path() / "max_state";
        auto cur_path = entry.path() / "cur_state";

        std::ifstream max_f(max_path);
        unsigned max_state = 0;
        max_f >> max_state;

        if (max_state == 0) continue;

        unsigned target = std::min(level, max_state);
        std::ofstream cur_f(cur_path);
        if (cur_f) cur_f << target;
    }
    } catch (const fs::filesystem_error&) {}
    return true;
}

} // namespace speedcool::pal
