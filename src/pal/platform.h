#pragma once

#include <string>

namespace speedcool::pal {

auto initialize_platform() -> bool;
auto get_os_info() -> std::string;

} // namespace speedcool::pal
