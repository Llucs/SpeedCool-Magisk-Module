#include "core/profile_manager.h"
#include "common/logging.h"
#include "pal/cpu/cpu_freq.h"
#include "pal/cpu/cpu_gov.h"
#include "pal/memory/vm_tuning.h"
#include "pal/io/io_scheduler.h"
#include "pal/gpu/gpu_info.h"
#include <filesystem>
#include <fstream>

namespace fs = std::filesystem;

namespace speedcool::profile {

ProfileManager::ProfileManager(const ProfileConfig profiles[5]) {
    for (int i = 0; i < 5; ++i) profiles_[i] = profiles[i];
}

auto ProfileManager::profile_name(ProfileType type) -> std::string {
    switch (type) {
        case ProfileType::Eco:         return "eco";
        case ProfileType::Balanced:    return "balanced";
        case ProfileType::Performance: return "performance";
        case ProfileType::Gaming:      return "gaming";
        case ProfileType::Custom:      return "custom";
        default:                       return "unknown";
    }
}

auto ProfileManager::name(ProfileType type) const -> std::string {
    return profile_name(type);
}

auto ProfileManager::current() const -> ProfileType {
    return current_;
}

auto ProfileManager::get_config(ProfileType type) const -> const ProfileConfig& {
    return profiles_[static_cast<u8>(type)];
}

auto ProfileManager::set_config(ProfileType type, const ProfileConfig& cfg) -> void {
    profiles_[static_cast<u8>(type)] = cfg;
}

auto ProfileManager::apply(ProfileType type) -> Result<void> {
    const auto& p = profiles_[static_cast<u8>(type)];
    log_info("Applying profile: {} ({})", profile_name(type), p.cpu_governor);

#ifdef __linux__
    return apply_linux(p);
#elif _WIN32
    return apply_windows(p);
#else
    return std::unexpected(err(Err::NotSupported, "Unsupported platform"));
#endif
}

auto ProfileManager::apply_linux(const ProfileConfig& p) -> Result<void> {
    if (!p.cpu_governor.empty()) {
        pal::set_all_governors(p.cpu_governor);
    }

    if (p.cpu_freq_limit_pct > 0 && p.cpu_freq_limit_pct < 100) {
        for (u32 cpu = 0; ; ++cpu) {
            auto max_path = std::format(
                "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_max_freq", cpu);
            if (!fs::exists(max_path)) break;

            std::ifstream ifs(
                std::format("/sys/devices/system/cpu/cpu{}/cpufreq/cpuinfo_max_freq", cpu));
            u32 max_freq = 0;
            ifs >> max_freq;

            if (max_freq > 0) {
                u32 limited = max_freq * p.cpu_freq_limit_pct / 100;
                std::ofstream ofs(max_path);
                if (ofs) ofs << limited;
            }
        }
    }

    pal::set_swappiness(p.swappiness);

    pal::IoController io;
    io.apply_profile_scheduler(p.io_scheduler);

    pal::GpuController gpu;
    if (gpu.governor() != "unknown") {
        gpu.set_governor(p.gpu_governor);
    }

    current_ = p.type;
    log_info("Profile {} applied successfully", profile_name(p.type));
    return {};
}

auto ProfileManager::apply_windows(const ProfileConfig& p) -> Result<void> {
    log_info("Windows profile application not yet implemented");
    current_ = p.type;
    return {};
}

} // namespace speedcool::profile
