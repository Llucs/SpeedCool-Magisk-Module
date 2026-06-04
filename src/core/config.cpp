#include "core/config.h"
#include "common/logging.h"
#include <fstream>
#include <filesystem>
#include <toml.hpp>

namespace fs = std::filesystem;

namespace speedcool::config {

static auto toml_to_profile(const toml::value& v) -> ProfileConfig {
    ProfileConfig p;
    if (v.contains("cpu_governor"))    p.cpu_governor = toml::find<std::string>(v, "cpu_governor");
    if (v.contains("cpu_freq_limit_pct")) p.cpu_freq_limit_pct = toml::find<u32>(v, "cpu_freq_limit_pct");
    if (v.contains("io_scheduler"))    p.io_scheduler = toml::find<std::string>(v, "io_scheduler");
    if (v.contains("gpu_governor"))    p.gpu_governor = toml::find<std::string>(v, "gpu_governor");
    if (v.contains("ram_clean_threshold_pct")) p.ram_clean_threshold_pct = toml::find<f64>(v, "ram_clean_threshold_pct");
    if (v.contains("swappiness"))      p.swappiness = toml::find<u32>(v, "swappiness");
    if (v.contains("power_scheme"))    p.power_scheme = toml::find<std::string>(v, "power_scheme");
    if (v.contains("process_priority")) p.process_priority = toml::find<std::string>(v, "process_priority");
    if (v.contains("mmcss_task"))      p.mmcss_task = toml::find<std::string>(v, "mmcss_task");
    if (v.contains("game_mode"))       p.game_mode = toml::find<bool>(v, "game_mode");
    if (v.contains("disable_core_parking")) p.disable_core_parking = toml::find<bool>(v, "disable_core_parking");
    return p;
}

auto get_config_path() -> std::string {
    const char* env = std::getenv("SPEEDCOOL_CONFIG");
    if (env) return env;

    std::vector<std::string> candidates = {
        "/etc/speedcool/speedcool.toml",
        std::format("{}/.config/speedcool/speedcool.toml", std::getenv("HOME") ? std::getenv("HOME") : "/root"),
        "speedcool.toml",
        "config/speedcool.toml"
    };

    for (auto& c : candidates) {
        if (fs::exists(c)) return c;
    }
    return candidates[0];
}

auto get_default_config() -> FullConfig {
    FullConfig cfg{};
    cfg.daemon.interval_sec = 300;
    cfg.daemon.log_level = "info";
    cfg.daemon.log_file = "/var/log/speedcool.log";
    cfg.daemon.adaptive_enabled = true;
    cfg.daemon.auto_update_check = true;
    cfg.daemon.update_channel = "stable";
    cfg.daemon.github_repo = "Llucs/SpeedCool-Magisk-Module";
    cfg.daemon.known_conflict_modules = {"lspeed", "magnetar", "rickthermal", "fde.ai"};
    cfg.daemon.auto_resolve_conflicts = true;
    cfg.daemon.telemetry_enabled = false;
    cfg.daemon.telemetry_retention_days = 30;

    cfg.adaptive.learning_rate = 0.01;
    cfg.adaptive.predictive_mode = true;

    cfg.profiles[0] = {ProfileType::Eco, "powersave", 70, "bfq", "powersave", 92.0, 10, "powersaver", "idle", "", false, false};
    cfg.profiles[1] = {ProfileType::Balanced, "schedutil", 0, "bfq", "simple_ondemand", 85.0, 60, "balanced", "below_normal", "", false, false};
    cfg.profiles[2] = {ProfileType::Performance, "performance", 100, "kyber", "performance", 75.0, 100, "high_performance", "high", "", false, false};
    cfg.profiles[3] = {ProfileType::Gaming, "performance", 100, "none", "performance", 70.0, 100, "ultimate_performance", "high", "Games", true, true};
    cfg.profiles[4] = {ProfileType::Custom, "schedutil", 0, "bfq", "simple_ondemand", 85.0, 60, "balanced", "below_normal", "", false, false};

    return cfg;
}

auto load(const std::string& path) -> Result<FullConfig> {
    FullConfig cfg = get_default_config();

    if (!fs::exists(path)) {
        log_warn("Config file not found at {}, using defaults", path);
        return cfg;
    }

    try {
        auto data = toml::parse(path);

        if (data.contains("daemon")) {
            auto& d = data.at("daemon");
            if (d.contains("interval_sec")) cfg.daemon.interval_sec = toml::find<u32>(d, "interval_sec");
            if (d.contains("log_level"))    cfg.daemon.log_level = toml::find<std::string>(d, "log_level");
            if (d.contains("log_file"))     cfg.daemon.log_file = toml::find<std::string>(d, "log_file");
        }
        if (data.contains("adaptive")) {
            auto& a = data.at("adaptive");
            if (a.contains("enabled"))       cfg.daemon.adaptive_enabled = toml::find<bool>(a, "enabled");
            if (a.contains("learning_rate")) cfg.adaptive.learning_rate = toml::find<f64>(a, "learning_rate");
            if (a.contains("predictive_mode")) cfg.adaptive.predictive_mode = toml::find<bool>(a, "predictive_mode");
        }
        if (data.contains("profile")) {
            auto& profiles = data.at("profile");
            for (auto& [key, val] : profiles.as_table()) {
                if (key == "eco")         cfg.profiles[0] = toml_to_profile(val);
                else if (key == "balanced") cfg.profiles[1] = toml_to_profile(val);
                else if (key == "performance") cfg.profiles[2] = toml_to_profile(val);
                else if (key == "gaming")  cfg.profiles[3] = toml_to_profile(val);
                else if (key == "custom")  cfg.profiles[4] = toml_to_profile(val);
            }
        }
        if (data.contains("conflict")) {
            auto& c = data.at("conflict");
            if (c.contains("known_modules")) {
                cfg.known_conflict_modules = toml::find<std::vector<std::string>>(c, "known_modules");
            }
            if (c.contains("auto_resolve")) cfg.daemon.auto_resolve_conflicts = toml::find<bool>(c, "auto_resolve");
        }
        if (data.contains("update")) {
            auto& u = data.at("update");
            if (u.contains("auto_check"))  cfg.daemon.auto_update_check = toml::find<bool>(u, "auto_check");
            if (u.contains("channel"))     cfg.daemon.update_channel = toml::find<std::string>(u, "channel");
            if (u.contains("github_repo")) cfg.daemon.github_repo = toml::find<std::string>(u, "github_repo");
        }
        if (data.contains("telemetry")) {
            auto& t = data.at("telemetry");
            if (t.contains("enabled"))         cfg.daemon.telemetry_enabled = toml::find<bool>(t, "enabled");
            if (t.contains("retention_days"))  cfg.daemon.telemetry_retention_days = toml::find<u32>(t, "retention_days");
        }

        log_info("Config loaded from {}", path);
    } catch (const toml::syntax_error& e) {
        log_error("TOML parse error in {}: {}", path, e.what());
        return std::unexpected(err(Err::ParseError, e.what()));
    } catch (const std::exception& e) {
        log_warn("Error loading config: {}", e.what());
    }

    return cfg;
}

auto save(const FullConfig& cfg, const std::string& path) -> Result<void> {
    try {
        toml::value data;

        data["daemon"]["interval_sec"] = cfg.daemon.interval_sec;
        data["daemon"]["log_level"] = cfg.daemon.log_level;
        data["daemon"]["log_file"] = cfg.daemon.log_file;

        data["adaptive"]["enabled"] = cfg.daemon.adaptive_enabled;
        data["adaptive"]["learning_rate"] = cfg.adaptive.learning_rate;
        data["adaptive"]["predictive_mode"] = cfg.adaptive.predictive_mode;

        auto save_profile = [&](const std::string& name, const ProfileConfig& p) {
            data["profile"][name]["cpu_governor"] = p.cpu_governor;
            data["profile"][name]["cpu_freq_limit_pct"] = p.cpu_freq_limit_pct;
            data["profile"][name]["io_scheduler"] = p.io_scheduler;
            data["profile"][name]["gpu_governor"] = p.gpu_governor;
            data["profile"][name]["ram_clean_threshold_pct"] = p.ram_clean_threshold_pct;
            data["profile"][name]["swappiness"] = p.swappiness;
            data["profile"][name]["power_scheme"] = p.power_scheme;
            data["profile"][name]["process_priority"] = p.process_priority;
            data["profile"][name]["game_mode"] = p.game_mode;
        };

        save_profile("eco", cfg.profiles[0]);
        save_profile("balanced", cfg.profiles[1]);
        save_profile("performance", cfg.profiles[2]);
        save_profile("gaming", cfg.profiles[3]);
        save_profile("custom", cfg.profiles[4]);

        data["conflict"]["known_modules"] = cfg.known_conflict_modules;
        data["conflict"]["auto_resolve"] = cfg.daemon.auto_resolve_conflicts;

        data["update"]["auto_check"] = cfg.daemon.auto_update_check;
        data["update"]["channel"] = cfg.daemon.update_channel;
        data["update"]["github_repo"] = cfg.daemon.github_repo;

        data["telemetry"]["enabled"] = cfg.daemon.telemetry_enabled;
        data["telemetry"]["retention_days"] = cfg.daemon.telemetry_retention_days;

        std::ofstream ofs(path);
        if (!ofs) {
            return std::unexpected(err(Err::IoError, std::format("Cannot write {}", path)));
        }
        ofs << data;
        log_info("Config saved to {}", path);
    } catch (const std::exception& e) {
        return std::unexpected(err(Err::ConfigError, e.what()));
    }

    return {};
}

} // namespace speedcool::config
