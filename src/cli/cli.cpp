#include "cli/cli.h"
#include "cli/tui.h"
#include "core/daemon.h"
#include "core/config.h"
#include "common/logging.h"
#include "common/types.h"
#include <print>
#include <string_view>
#include <fstream>

namespace speedcool::cli {

auto Cli::run(int argc, char* argv[]) -> int {
    if (argc < 2) return print_help();

    std::string_view cmd = argv[1];

    if (cmd == "status")       return cmd_status();
    if (cmd == "perf")         return cmd_perf(argc, argv);
    if (cmd == "monitor")      return cmd_monitor();
    if (cmd == "history")      return cmd_history();
    if (cmd == "update")       return cmd_update(argc, argv);
    if (cmd == "help" || cmd == "--help" || cmd == "-h") return print_help();

    std::println("Unknown command: {}", cmd);
    return print_help();
}

auto Cli::cmd_status() -> int {
    DaemonConfig dc{};
    auto cfg = config::load(config::get_config_path());
    if (cfg) {
        std::println("SpeedCool C++26 v{}", SPEEDCOOL_VERSION);
        std::println("Config: {}", config::get_config_path());
        std::println("Interval: {}s", cfg->daemon.interval_sec);
        std::println("Adaptive: {}", cfg->daemon.adaptive_enabled ? "enabled" : "disabled");
        std::println("Log level: {}", cfg->daemon.log_level);
        std::println("Update channel: {}", cfg->daemon.update_channel);
        std::println("Auto-update: {}", cfg->daemon.auto_update_check ? "on" : "off");
        std::println("Telemetry: {}", cfg->daemon.telemetry_enabled ? "on" : "off");
    } else {
        std::println("Error loading config: {}", cfg.error().message());
        return 1;
    }
    return 0;
}

auto Cli::cmd_perf(int argc, char* argv[]) -> int {
    if (argc < 3) {
        std::println("Usage: speedcool perf set <profile>");
        std::println("       speedcool perf auto on|off");
        std::println("Profiles: eco, balanced, performance, gaming, custom");
        return 1;
    }

    std::string_view sub = argv[2];

    if (sub == "set" && argc >= 4) {
        std::string_view profile_name = argv[3];
        ProfileType type;
        if (profile_name == "eco")         type = ProfileType::Eco;
        else if (profile_name == "balanced") type = ProfileType::Balanced;
        else if (profile_name == "performance") type = ProfileType::Performance;
        else if (profile_name == "gaming")  type = ProfileType::Gaming;
        else if (profile_name == "custom")  type = ProfileType::Custom;
        else {
            std::println("Unknown profile: {}", profile_name);
            return 1;
        }

        core::Daemon daemon;
        auto r = daemon.initialize(config::get_config_path());
        if (!r) {
            std::println("Error: {}", r.error().message());
            return 1;
        }
        r = daemon.force_profile(type);
        if (!r) {
            std::println("Error applying profile: {}", r.error().message());
            return 1;
        }
        std::println("Profile set to {}", profile_name);
    } else if (sub == "auto" && argc >= 4) {
        std::string_view mode = argv[3];
        core::Daemon daemon;
        auto r = daemon.initialize(config::get_config_path());
        if (r) {
            daemon.set_adaptive(mode == "on");
            std::println("Adaptive engine turned {}", mode);
        }
    } else {
        std::println("Usage: speedcool perf set <profile>");
        std::println("       speedcool perf auto on|off");
    }
    return 0;
}

auto Cli::cmd_monitor() -> int {
    run_tui();
    return 0;
}

auto Cli::cmd_history() -> int {
    std::println("History: not yet implemented");
    return 0;
}

auto Cli::cmd_update(int argc, char* argv[]) -> int {
    if (argc < 3) {
        std::println("Usage: speedcool update check");
        std::println("       speedcool update apply");
        return 1;
    }
    std::string_view sub = argv[2];
    if (sub == "check") {
        std::println("Update check: not yet implemented");
    } else if (sub == "apply") {
        std::println("Update apply: not yet implemented");
    }
    return 0;
}

auto Cli::print_help() -> int {
    std::println("SpeedCool C++26 v{}", SPEEDCOOL_VERSION);
    std::println("Usage: speedcool <command> [options]");
    std::println("");
    std::println("Commands:");
    std::println("  status                    Show system status");
    std::println("  perf set <profile>       Force performance profile");
    std::println("  perf auto on|off         Enable/disable adaptive engine");
    std::println("  monitor                  Open TUI dashboard");
    std::println("  history                  Show last 24h history");
    std::println("  update check             Check for updates");
    std::println("  update apply             Apply update");
    std::println("  help                     Show this help");
    std::println("");
    std::println("Profiles: eco, balanced, performance, gaming, custom");
    return 0;
}

} // namespace speedcool::cli
