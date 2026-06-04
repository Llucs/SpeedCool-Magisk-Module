#include "common/types.h"
#include "common/logging.h"
#include "pal/platform.h"
#include "pal/cpu/cpu_info.h"
#include "pal/cpu/cpu_freq.h"
#include "pal/cpu/cpu_gov.h"
#include "pal/memory/ram_info.h"
#include "pal/memory/vm_tuning.h"
#include "pal/io/io_scheduler.h"
#include "pal/thermal/thermal.h"
#include "pal/gpu/gpu_info.h"
#include "pal/power/power.h"
#include <fstream>

namespace speedcool::pal {

auto initialize_platform() -> bool {
    log_info("Initializing Linux platform backend");

    auto topo = detect_cpu_topology();
    log_info("System: {} cores ({} logical), {} sockets, hybrid={}",
             topo.physical_cores, topo.logical_cores, topo.sockets, topo.hybrid);

    return true;
}

auto get_os_info() -> std::string {
    std::string info;
    std::ifstream ifs("/etc/os-release");
    if (ifs) {
        std::string line;
        while (std::getline(ifs, line)) {
            if (line.starts_with("PRETTY_NAME=")) {
                info = line.substr(line.find('=') + 1);
                if (info.size() >= 2 && info.front() == '"' && info.back() == '"') {
                    info = info.substr(1, info.size() - 2);
                }
                break;
            }
        }
    }
    return info;
}

} // namespace speedcool::pal
