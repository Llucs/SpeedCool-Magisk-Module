#include "common/types.h"
#include "pal/memory/ram_info.h"
#include "pal/memory/vm_tuning.h"
#include <print>

using namespace speedcool;

auto test_ram_info() -> int {
    auto mem = pal::get_memory_info();
    std::println("  RAM: {}GB total, {}GB available ({:.1f}%)",
                 mem.total_ram, mem.available_ram, mem.usage_percent);

    if (mem.total_ram == 0) {
        std::println("  FAIL: No RAM detected");
        return 1;
    }
    if (mem.usage_percent < 0 || mem.usage_percent > 100) {
        std::println("  FAIL: Invalid RAM usage percent");
        return 1;
    }
    std::println("  PASS");
    return 0;
}

auto test_vm_params() -> int {
    auto swappiness = pal::get_swappiness();
    auto cache_pressure = pal::get_vfs_cache_pressure();
    std::println("  swappiness={}, vfs_cache_pressure={}", swappiness, cache_pressure);

    if (swappiness == 0 && cache_pressure == 0) {
        std::println("  WARN: Could not read VM params (may need root)");
    }
    std::println("  PASS");
    return 0;
}

auto main() -> int {
    int failed = 0;
    std::println("=== Memory Tests ===");

    std::println("--- test_ram_info ---");
    failed += test_ram_info();

    std::println("--- test_vm_params ---");
    failed += test_vm_params();

    if (failed > 0) {
        std::println("FAILED: {} tests", failed);
    } else {
        std::println("ALL MEMORY TESTS PASSED");
    }
    return failed > 0 ? 1 : 0;
}
