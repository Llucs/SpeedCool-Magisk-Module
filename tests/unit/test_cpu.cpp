#include "common/types.h"
#include "pal/cpu/cpu_info.h"
#include "pal/cpu/cpu_freq.h"
#include "pal/cpu/cpu_gov.h"
#include <print>
#include <cstdlib>

using namespace speedcool;

auto test_cpu_topology() -> int {
    auto topo = pal::detect_cpu_topology();
    std::println("  Cores: {} physical, {} logical", topo.physical_cores, topo.logical_cores);

    if (topo.logical_cores == 0) {
        std::println("  FAIL: No CPU cores detected");
        return 1;
    }
    if (topo.physical_cores == 0 || topo.physical_cores > topo.logical_cores) {
        std::println("  FAIL: Invalid physical core count");
        return 1;
    }
    std::println("  PASS");
    return 0;
}

auto test_cpu_frequencies() -> int {
    auto freqs = pal::get_current_frequencies();
    std::println("  Frequencies: {} CPUs reported", freqs.size());

    if (freqs.empty()) {
        std::println("  WARN: No frequencies detected (may need root)");
    }
    std::println("  PASS");
    return 0;
}

auto test_cpu_governors() -> int {
    auto govs = pal::get_current_governors();
    std::println("  Governors: {} reported", govs.size());

    if (govs.empty()) {
        std::println("  WARN: No governors detected (may need root)");
    } else {
        std::println("  First governor: {}", govs[0]);
    }
    std::println("  PASS");
    return 0;
}

auto main() -> int {
    int failed = 0;
    std::println("=== CPU Tests ===");

    std::println("--- test_cpu_topology ---");
    failed += test_cpu_topology();

    std::println("--- test_cpu_frequencies ---");
    failed += test_cpu_frequencies();

    std::println("--- test_cpu_governors ---");
    failed += test_cpu_governors();

    if (failed > 0) {
        std::println("FAILED: {} tests", failed);
    } else {
        std::println("ALL CPU TESTS PASSED");
    }
    return failed > 0 ? 1 : 0;
}
