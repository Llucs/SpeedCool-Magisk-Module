#pragma once

#include "common/types.h"

namespace speedcool::pal {

auto get_power_info() -> PowerInfo;
auto get_battery_level() -> Percent;
auto is_on_ac() -> bool;
auto set_wake_lock(bool enable, const std::string& tag = "speedcool") -> bool;

} // namespace speedcool::pal
