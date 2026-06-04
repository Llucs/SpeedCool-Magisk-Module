#include "core/profile_manager.h"
#include "core/config.h"
#include <print>

using namespace speedcool;

auto test_profile_name() -> int {
    if (profile::ProfileManager::profile_name(ProfileType::Eco) != "eco") {
        std::println("  FAIL: Expected eco");
        return 1;
    }
    if (profile::ProfileManager::profile_name(ProfileType::Performance) != "performance") {
        std::println("  FAIL: Expected performance");
        return 1;
    }
    if (profile::ProfileManager::profile_name(ProfileType::Gaming) != "gaming") {
        std::println("  FAIL: Expected gaming");
        return 1;
    }
    std::println("  PASS");
    return 0;
}

auto test_profile_config() -> int {
    auto def = config::get_default_config();
    profile::ProfileManager mgr(def.profiles);

    auto& eco = mgr.get_config(ProfileType::Eco);
    std::println("  Eco: governor={}, swappiness={}", eco.cpu_governor, eco.swappiness);

    if (eco.cpu_governor != "powersave") {
        std::println("  FAIL: Expected powersave governor");
        return 1;
    }
    if (eco.swappiness != 10) {
        std::println("  FAIL: Expected swappiness=10");
        return 1;
    }

    auto& gaming = mgr.get_config(ProfileType::Gaming);
    std::println("  Gaming: governor={}, io={}, game_mode={}",
                 gaming.cpu_governor, gaming.io_scheduler, gaming.game_mode);

    if (gaming.io_scheduler != "none") {
        std::println("  FAIL: Expected none I/O scheduler");
        return 1;
    }
    if (!gaming.game_mode) {
        std::println("  FAIL: Expected game_mode=true");
        return 1;
    }

    std::println("  PASS");
    return 0;
}

auto test_set_config() -> int {
    auto def = config::get_default_config();
    profile::ProfileManager mgr(def.profiles);

    ProfileConfig custom{};
    custom.cpu_governor = "ondemand";
    custom.swappiness = 50;
    custom.type = ProfileType::Custom;

    mgr.set_config(ProfileType::Custom, custom);

    auto& retrieved = mgr.get_config(ProfileType::Custom);
    if (retrieved.cpu_governor != "ondemand") {
        std::println("  FAIL: Expected ondemand governor");
        return 1;
    }

    std::println("  PASS");
    return 0;
}

auto main() -> int {
    int failed = 0;
    std::println("=== Profile Manager Tests ===");

    std::println("--- test_profile_name ---");
    failed += test_profile_name();

    std::println("--- test_profile_config ---");
    failed += test_profile_config();

    std::println("--- test_set_config ---");
    failed += test_set_config();

    if (failed > 0) {
        std::println("FAILED: {} profile tests", failed);
    } else {
        std::println("ALL PROFILE MANAGER TESTS PASSED");
    }
    return failed > 0 ? 1 : 0;
}
