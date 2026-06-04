#include "common/logging.h"
#include "common/types.h"
#include "core/daemon.h"
#include "core/config.h"
#include "cli/cli.h"
#include <print>
#include <csignal>

namespace {
    std::unique_ptr<speedcool::core::Daemon> g_daemon;

    void signal_handler(int sig) {
        std::println("Signal {} received, shutting down...", sig);
        if (g_daemon) g_daemon->stop();
    }
}

auto main(int argc, char* argv[]) -> int {
    std::signal(SIGINT, signal_handler);
    std::signal(SIGTERM, signal_handler);

    speedcool::cli::Cli cli;

    if (argc > 1) {
        return cli.run(argc, argv);
    }

    auto config_path = speedcool::config::get_config_path();
    g_daemon = std::make_unique<speedcool::core::Daemon>();

    auto result = g_daemon->initialize(config_path);
    if (!result) {
        std::println("{}", result.error().message());
        speedcool::log_error("Initialization failed: {}", result.error().message());
        return 1;
    }

    g_daemon->run();
    speedcool::log_info("Daemon running. PID: {}", getpid());

    std::println("SpeedCool C++26 daemon running. Press Ctrl+C to stop.");

    while (g_daemon->is_running()) {
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }

    speedcool::log_info("Daemon exiting");
    return 0;
}
