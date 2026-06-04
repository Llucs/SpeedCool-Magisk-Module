#pragma once

#include "common/types.h"

namespace speedcool::pal {

auto set_swappiness(u32 value) -> bool;
auto set_vfs_cache_pressure(u32 value) -> bool;
auto set_drop_caches(u32 level) -> bool;
auto get_swappiness() -> u32;
auto get_vfs_cache_pressure() -> u32;

} // namespace speedcool::pal
