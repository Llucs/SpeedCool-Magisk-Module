#include "pal/io/io_scheduler.h"
#include "common/logging.h"
#include <fstream>
#include <filesystem>
#include <sstream>

namespace fs = std::filesystem;

namespace speedcool::pal {

auto IoController::set_scheduler(const std::string& dev, const std::string& sched) -> bool {
    auto path = std::format("/sys/block/{}/queue/scheduler", dev);
    std::ofstream ofs(path);
    if (!ofs) {
        log_warn("Cannot set scheduler for {} to {}", dev, sched);
        return false;
    }
    ofs << sched;
    log_info("I/O scheduler for {} set to {}", dev, sched);
    return true;
}

auto IoController::set_read_ahead(const std::string& dev, KB kb) -> bool {
    auto path = std::format("/sys/block/{}/queue/read_ahead_kb", dev);
    std::ofstream ofs(path);
    if (!ofs) return false;
    ofs << kb;
    log_debug("Read-ahead for {} set to {}KB", dev, kb);
    return true;
}

auto IoController::set_nr_requests(const std::string& dev, u32 n) -> bool {
    auto path = std::format("/sys/block/{}/queue/nr_requests", dev);
    std::ofstream ofs(path);
    if (!ofs) return false;
    ofs << n;
    return true;
}

auto IoController::devices() -> std::vector<BlkDevice> {
    std::vector<BlkDevice> devs;
    for (auto& entry : fs::directory_iterator("/sys/block")) {
        auto name = entry.path().filename().string();
        if (name.starts_with("loop") || name.starts_with("ram") || name.starts_with("zram")) continue;

        BlkDevice dev;
        dev.name = name;
        dev.nvme = name.starts_with("nvme");

        std::ifstream rot(entry.path() / "queue" / "rotational");
        rot >> dev.rotational;

        std::ifstream ra(entry.path() / "queue" / "read_ahead_kb");
        ra >> dev.read_ahead_kb;

        std::ifstream sched(entry.path() / "queue" / "scheduler");
        std::string s;
        std::getline(sched, s);
        auto open_bracket = s.find('[');
        auto close_bracket = s.find(']');
        if (open_bracket != std::string::npos && close_bracket != std::string::npos) {
            dev.scheduler = s.substr(open_bracket + 1, close_bracket - open_bracket - 1);
        } else if (!s.empty()) {
            dev.scheduler = s;
        }

        devs.push_back(dev);
    }
    return devs;
}

auto IoController::apply_profile_scheduler(const std::string& sched) -> bool {
    bool ok = true;
    for (auto& dev : devices()) {
        if (dev.nvme) {
            ok &= set_scheduler(dev.name, "none");
        } else if (!dev.rotational) {
            ok &= set_scheduler(dev.name, sched == "none" ? "kyber" : sched);
        } else {
            ok &= set_scheduler(dev.name, sched == "none" ? "bfq" : sched);
        }
    }
    return ok;
}

} // namespace speedcool::pal
