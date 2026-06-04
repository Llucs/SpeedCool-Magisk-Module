#pragma once

#include "common/types.h"
#include "common/error.h"
#include <string>
#include <unordered_map>

namespace speedcool::config {

struct FullConfig {
    DaemonConfig daemon;
    AdaptiveState adaptive;
    ProfileConfig profiles[5];
    std::vector<std::string> known_conflict_modules;
};

auto load(const std::string& path) -> Result<FullConfig>;
auto save(const FullConfig& cfg, const std::string& path) -> Result<void>;
auto get_default_config() -> FullConfig;
auto get_config_path() -> std::string;

} // namespace speedcool::config
