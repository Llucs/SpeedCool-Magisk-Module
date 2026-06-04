#include "pal/gpu/gpu_info.h"
#include "common/logging.h"
#include <fstream>
#include <filesystem>
#include <algorithm>

namespace fs = std::filesystem;

namespace speedcool::pal {

auto GpuController::current_freq_mhz() -> MHz {
    if (fs::exists("/sys/class/kgsl/kgsl-3d0/gpuclk")) {
        std::ifstream ifs("/sys/class/kgsl/kgsl-3d0/gpuclk");
        MHz freq = 0;
        if (ifs >> freq) return freq / 1000;
    }
    if (fs::exists("/sys/kernel/gpu/gpu_clock")) {
        std::ifstream ifs("/sys/kernel/gpu/gpu_clock");
        MHz freq = 0;
        if (ifs >> freq) return freq;
    }
    if (fs::exists("/sys/class/drm/card0/gt_cur_freq_mhz")) {
        std::ifstream ifs("/sys/class/drm/card0/gt_cur_freq_mhz");
        MHz freq = 0;
        if (ifs >> freq) return freq;
    }
    return 0;
}

auto GpuController::temperature_celsius() -> Celsius {
    if (fs::exists("/sys/class/kgsl/kgsl-3d0/temp")) {
        std::ifstream ifs("/sys/class/kgsl/kgsl-3d0/temp");
        i32 raw = 0;
        if (ifs >> raw) return raw / 100.0;
    }
    if (fs::exists("/sys/class/drm/card0/hwmon/hwmon0/temp1_input")) {
        std::ifstream ifs("/sys/class/drm/card0/hwmon/hwmon0/temp1_input");
        i32 raw = 0;
        if (ifs >> raw) return raw / 1000.0;
    }
    return 0;
}

auto GpuController::governor() -> std::string {
    if (fs::exists("/sys/class/kgsl/kgsl-3d0/devfreq/governor")) {
        std::ifstream ifs("/sys/class/kgsl/kgsl-3d0/devfreq/governor");
        std::string gov;
        ifs >> gov;
        return gov;
    }
    return "unknown";
}

auto GpuController::set_governor(const std::string& gov) -> bool {
    if (fs::exists("/sys/class/kgsl/kgsl-3d0/devfreq/governor")) {
        std::ofstream ofs("/sys/class/kgsl/kgsl-3d0/devfreq/governor");
        if (!ofs) return false;
        ofs << gov;
        log_info("GPU governor set to {}", gov);
        return true;
    }
    return false;
}

auto GpuController::is_active() -> bool {
    auto freq = current_freq_mhz();
    if (freq > 0) return true;

    if (fs::exists("/sys/class/kgsl/kgsl-3d0/busy")) {
        std::ifstream ifs("/sys/class/kgsl/kgsl-3d0/busy");
        u64 busy = 0;
        ifs >> busy;
        return busy > 10000;
    }
    return false;
}

} // namespace speedcool::pal
