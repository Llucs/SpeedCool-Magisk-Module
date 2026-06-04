#include "update/manifest.h"
#include "common/logging.h"
#include <fstream>
#include <filesystem>
#include <nlohmann/json.hpp>

using json = nlohmann::json;

namespace fs = std::filesystem;

namespace speedcool::update {

auto load_manifest(const std::string& path) -> Manifest {
    Manifest m{};
    if (!fs::exists(path)) return m;

    try {
        std::ifstream ifs(path);
        json j;
        ifs >> j;

        m.version = j.value("version", "");
        m.system_id = j.value("system_id", "");

        if (j.contains("entries")) {
            for (auto& e : j["entries"]) {
                m.entries.push_back({
                    e["metric_name"],
                    e["value"],
                    e["timestamp"]
                });
            }
        }
    } catch (const std::exception& e) {
        log_warn("Error loading manifest: {}", e.what());
    }

    return m;
}

auto save_manifest(const Manifest& m, const std::string& path) -> void {
    try {
        json j;
        j["version"] = m.version;
        j["system_id"] = m.system_id;

        json entries = json::array();
        for (auto& e : m.entries) {
            entries.push_back({
                {"metric_name", e.metric_name},
                {"value", e.value},
                {"timestamp", e.timestamp}
            });
        }
        j["entries"] = entries;

        auto dir = fs::path(path).parent_path();
        if (!dir.empty()) {
            fs::create_directories(dir);
        }

        std::ofstream ofs(path);
        ofs << j.dump(2);
    } catch (const std::exception& e) {
        log_warn("Error saving manifest: {}", e.what());
    }
}

} // namespace speedcool::update
