#pragma once

#include "common/types.h"
#include <string>

namespace speedcool::pal {

struct GpuController {
    auto current_freq_mhz() -> MHz;
    auto temperature_celsius() -> Celsius;
    auto governor() -> std::string;
    auto set_governor(const std::string& gov) -> bool;
    auto is_active() -> bool;
};

} // namespace speedcool::pal
