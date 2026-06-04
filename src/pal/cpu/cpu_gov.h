#pragma once

#include "common/types.h"
#include <string>

namespace speedcool::pal {

auto get_current_governors() -> std::vector<std::string>;
auto set_all_governors(const std::string& gov) -> bool;
auto get_governor_stats(const std::string& gov) -> std::string;

} // namespace speedcool::pal
