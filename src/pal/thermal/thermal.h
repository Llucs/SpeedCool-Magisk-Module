#pragma once

#include "common/types.h"
#include <vector>

namespace speedcool::pal {

struct ThermalController {
    auto cpu_temp_celsius() -> Celsius;
    auto zones() -> std::vector<ThermalZone>;
    auto set_cooling_level(unsigned level) -> bool;
};

} // namespace speedcool::pal
