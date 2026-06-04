#pragma once

#include "common/types.h"

namespace speedcool::pal {

auto get_memory_info() -> MemoryInfo;
auto get_hugepages_info() -> std::string;

} // namespace speedcool::pal
