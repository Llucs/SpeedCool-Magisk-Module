#include "pal/memory/vm_tuning.h"
#include "common/logging.h"
#include <fstream>

namespace speedcool::pal {

auto write_proc_sys(const std::string& path, u32 value) -> bool {
    std::ofstream ofs(path);
    if (!ofs) {
        log_warn("Cannot write {} = {}", path, value);
        return false;
    }
    ofs << value;
    return true;
}

auto read_proc_sys_u32(const std::string& path) -> u32 {
    std::ifstream ifs(path);
    if (!ifs) return 0;
    u32 val;
    ifs >> val;
    return val;
}

auto set_swappiness(u32 value) -> bool {
    return write_proc_sys("/proc/sys/vm/swappiness", value);
}

auto set_vfs_cache_pressure(u32 value) -> bool {
    return write_proc_sys("/proc/sys/vm/vfs_cache_pressure", value);
}

auto set_drop_caches(u32 level) -> bool {
    std::ofstream ofs("/proc/sys/vm/drop_caches");
    if (!ofs) {
        log_warn("Cannot drop caches (level {})", level);
        return false;
    }
    ofs << level;
    if (level > 0) log_info("Dropped caches (level {})", level);
    return true;
}

auto get_swappiness() -> u32 {
    return read_proc_sys_u32("/proc/sys/vm/swappiness");
}

auto get_vfs_cache_pressure() -> u32 {
    return read_proc_sys_u32("/proc/sys/vm/vfs_cache_pressure");
}

} // namespace speedcool::pal
