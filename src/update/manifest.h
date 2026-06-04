#pragma once

#include "common/types.h"
#include <string>
#include <vector>

namespace speedcool::update {

struct ManifestEntry {
    std::string metric_name;
    f64 value{};
    u64 timestamp{};
};

struct Manifest {
    std::string version;
    std::string system_id;
    std::vector<ManifestEntry> entries;
};

auto load_manifest(const std::string& path) -> Manifest;
auto save_manifest(const Manifest& m, const std::string& path) -> void;

} // namespace speedcool::update
