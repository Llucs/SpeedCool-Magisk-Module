#include "common/types.h"
#include "common/logging.h"

namespace speedcool::pal {

auto initialize_platform() -> bool {
    log_info("Initializing Windows platform backend");
    return true;
}

auto get_os_info() -> std::string {
    return "Windows (stub)";
}

} // namespace speedcool::pal
