#pragma once

#include "common/types.h"
#include <vector>

namespace speedcool::pal {

auto get_current_frequencies() -> std::vector<MHz>;
auto get_available_frequencies(u32 cpu) -> std::vector<MHz>;
auto get_available_governors(u32 cpu) -> std::vector<std::string>;
auto set_frequency_limits(u32 cpu, MHz min, MHz max) -> bool;
auto set_governor(u32 cpu, const std::string& gov) -> bool;
auto set_energy_perf_policy(const std::string& policy) -> bool;

} // namespace speedcool::pal
