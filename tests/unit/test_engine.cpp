#include "core/engine.h"
#include "common/types.h"
#include <print>

using namespace speedcool;

auto test_eco_at_night() -> int {
    DaemonConfig cfg{};
    Metrics m{};
    m.hour = 3;
    m.cpu_usage = 10.0;
    m.ram_percent = 30.0;

    auto decision = engine::decide(m, cfg);
    std::println("  Hour={} -> {}", m.hour, static_cast<int>(decision.profile));

    if (decision.profile != ProfileType::Eco) {
        std::println("  FAIL: Expected Eco at night, got {}", static_cast<int>(decision.profile));
        return 1;
    }
    std::println("  PASS");
    return 0;
}

auto test_gaming_detection() -> int {
    DaemonConfig cfg{};
    Metrics m{};
    m.hour = 20;
    m.cpu_usage = 65.0;
    m.gpu_active = true;

    auto decision = engine::decide(m, cfg);
    std::println("  GPU active, CPU={}% -> {}", m.cpu_usage, static_cast<int>(decision.profile));

    if (decision.profile != ProfileType::Gaming) {
        std::println("  FAIL: Expected Gaming, got {}", static_cast<int>(decision.profile));
        return 1;
    }
    std::println("  PASS");
    return 0;
}

auto test_performance_under_load() -> int {
    DaemonConfig cfg{};
    Metrics m{};
    m.hour = 14;
    m.cpu_usage = 85.0;
    m.ram_percent = 70.0;

    auto decision = engine::decide(m, cfg);
    std::println("  CPU={}%, RAM={}% -> {}", m.cpu_usage, m.ram_percent, static_cast<int>(decision.profile));

    if (decision.profile != ProfileType::Performance) {
        std::println("  FAIL: Expected Performance, got {}", static_cast<int>(decision.profile));
        return 1;
    }
    std::println("  PASS");
    return 0;
}

auto test_balanced_moderate() -> int {
    DaemonConfig cfg{};
    Metrics m{};
    m.hour = 14;
    m.cpu_usage = 45.0;
    m.ram_percent = 50.0;

    auto decision = engine::decide(m, cfg);
    std::println("  CPU={}%, RAM={}% -> {}", m.cpu_usage, m.ram_percent, static_cast<int>(decision.profile));

    if (decision.profile != ProfileType::Balanced) {
        std::println("  FAIL: Expected Balanced, got {}", static_cast<int>(decision.profile));
        return 1;
    }
    std::println("  PASS");
    return 0;
}

auto test_conflict_resolution() -> int {
    DaemonConfig cfg{};
    cfg.auto_resolve_conflicts = true;
    Metrics m{};
    m.conflito_detectado = true;

    auto decision = engine::decide(m, cfg);
    std::println("  Conflict -> {}", static_cast<int>(decision.profile));

    if (decision.profile != ProfileType::Balanced) {
        std::println("  FAIL: Expected Balanced on conflict, got {}", static_cast<int>(decision.profile));
        return 1;
    }
    std::println("  PASS");
    return 0;
}

auto test_predictor() -> int {
    engine::Predictor pred(0.1);
    Metrics m{};
    m.cpu_usage = 50.0;
    m.ram_percent = 50.0;
    m.cpu_temp = 50.0;
    m.battery_level = 50.0;

    auto prediction = pred.predict(m);
    std::println("  Prediction: {}", prediction);

    if (prediction <= 0 || prediction > 1.0) {
        std::println("  FAIL: Prediction out of range");
        return 1;
    }

    pred.train(m, 0.8);
    auto prediction2 = pred.predict(m);
    std::println("  After training: {}", prediction2);

    auto w = pred.weights();
    std::println("  Weights: {:.4f} {:.4f} {:.4f} {:.4f}", w[0], w[1], w[2], w[3]);

    std::println("  PASS");
    return 0;
}

auto main() -> int {
    int failed = 0;
    std::println("=== Engine Tests ===");

    std::println("--- test_eco_at_night ---");
    failed += test_eco_at_night();

    std::println("--- test_gaming_detection ---");
    failed += test_gaming_detection();

    std::println("--- test_performance_under_load ---");
    failed += test_performance_under_load();

    std::println("--- test_balanced_moderate ---");
    failed += test_balanced_moderate();

    std::println("--- test_conflict_resolution ---");
    failed += test_conflict_resolution();

    std::println("--- test_predictor ---");
    failed += test_predictor();

    if (failed > 0) {
        std::println("FAILED: {} engine tests", failed);
    } else {
        std::println("ALL ENGINE TESTS PASSED");
    }
    return failed > 0 ? 1 : 0;
}
