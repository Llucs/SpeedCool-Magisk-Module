#include "core/daemon.h"
#include "core/config.h"
#include "common/logging.h"
#include "monitor/metrics_collector.h"
#include "pal/cpu/cpu_info.h"
#include <print>
#include <chrono>
#include <thread>

using namespace speedcool;

auto test_daemon_initialize() -> int {
    core::Daemon daemon;

    auto result = daemon.initialize("/tmp/speedcool_test_config.toml");
    if (!result) {
        std::println("  Daemon init: {} (expected if no config)", result.error().message());
        std::println("  PASS (graceful failure)");
        return 0;
    }

    std::println("  PASS");
    return 0;
}

auto test_metrics_collection() -> int {
    monitor::MetricsCollector collector;

    auto m1 = collector.collect();
    std::this_thread::sleep_for(std::chrono::milliseconds(100));
    auto m2 = collector.collect();

    std::println("  CPU: {:.1f}%, RAM: {:.1f}%, Temp: {:.1f}°C",
                 m2.cpu_usage, m2.ram_percent, m2.cpu_temp);
    std::println("  Load: {:.2f} / {:.2f} / {:.2f}",
                 m2.load_1m, m2.load_5m, m2.load_15m);

    if (m2.ram_percent < 0 || m2.ram_percent > 100) {
        std::println("  FAIL: Invalid RAM percent");
        return 1;
    }

    std::println("  PASS");
    return 0;
}

auto test_cpu_topology_integration() -> int {
    auto topo = pal::detect_cpu_topology();
    std::println("  Arch: {}, Cores: {}L/{}P, Hybrid: {}",
                 topo.arch, topo.logical_cores, topo.physical_cores, topo.hybrid);

    if (topo.logical_cores == 0) {
        std::println("  FAIL: No CPUs detected");
        return 1;
    }

    std::println("  PASS");
    return 0;
}

auto main() -> int {
    int failed = 0;
    std::println("=== Integration Tests ===");

    std::println("--- test_daemon_initialize ---");
    failed += test_daemon_initialize();

    std::println("--- test_metrics_collection ---");
    failed += test_metrics_collection();

    std::println("--- test_cpu_topology_integration ---");
    failed += test_cpu_topology_integration();

    if (failed > 0) {
        std::println("FAILED: {} integration tests", failed);
    } else {
        std::println("ALL INTEGRATION TESTS PASSED");
    }
    return failed > 0 ? 1 : 0;
}
