#include "core/config.h"
#include "common/error.h"
#include <print>
#include <filesystem>
#include <fstream>

using namespace speedcool;

auto test_default_config() -> int {
    auto cfg = config::get_default_config();
    std::println("  Interval: {}s, Adaptive: {}",
                 cfg.daemon.interval_sec, cfg.daemon.adaptive_enabled);

    if (cfg.daemon.interval_sec == 0) {
        std::println("  FAIL: Default interval is 0");
        return 1;
    }
    if (cfg.daemon.github_repo.empty()) {
        std::println("  FAIL: Default repo is empty");
        return 1;
    }
    std::println("  PASS");
    return 0;
}

auto test_config_load() -> int {
    auto tmp_path = "/tmp/speedcool_test_config.toml";

    {
        std::ofstream ofs(tmp_path);
        ofs << R"(
[daemon]
interval_sec = 60
log_level = "debug"

[adaptive]
enabled = false

[profile.eco]
cpu_governor = "powersave"
swappiness = 10

[profile.gaming]
cpu_governor = "performance"
io_scheduler = "none"
game_mode = true
)";
    }

    auto result = config::load(tmp_path);
    if (!result) {
        std::println("  FAIL: {}", result.error().message());
        return 1;
    }

    auto& cfg = *result;
    if (cfg.daemon.interval_sec != 60) {
        std::println("  FAIL: Expected interval 60, got {}", cfg.daemon.interval_sec);
        return 1;
    }
    if (cfg.daemon.adaptive_enabled != false) {
        std::println("  FAIL: Expected adaptive=false");
        return 1;
    }
    if (cfg.profiles[3].cpu_governor != "performance") {
        std::println("  FAIL: Expected gaming cpu_governor=performance");
        return 1;
    }
    if (cfg.profiles[3].game_mode != true) {
        std::println("  FAIL: Expected gaming game_mode=true");
        return 1;
    }

    std::filesystem::remove(tmp_path);
    std::println("  PASS");
    return 0;
}

auto main() -> int {
    int failed = 0;
    std::println("=== Config Tests ===");

    std::println("--- test_default_config ---");
    failed += test_default_config();

    std::println("--- test_config_load ---");
    failed += test_config_load();

    if (failed > 0) {
        std::println("FAILED: {} tests", failed);
    } else {
        std::println("ALL CONFIG TESTS PASSED");
    }
    return failed > 0 ? 1 : 0;
}
