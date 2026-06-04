#include "cli/tui.h"
#include "common/logging.h"
#include "common/types.h"
#include "pal/cpu/cpu_freq.h"
#include "pal/memory/ram_info.h"
#include "pal/thermal/thermal.h"
#include "pal/power/power.h"
#include "pal/gpu/gpu_info.h"
#include "monitor/metrics_collector.h"

#include <ftxui/dom/elements.hpp>
#include <ftxui/screen/screen.hpp>
#include <ftxui/component/screen_interactive.hpp>
#include <ftxui/component/component.hpp>
#include <ftxui/component/component_options.hpp>
#include <thread>
#include <chrono>

using namespace ftxui;

namespace speedcool::cli {

auto format_bar(Percent value, int width = 20) -> Element {
    int filled = static_cast<int>(value * width / 100.0);
    filled = std::clamp(filled, 0, width);

    Color bar_color = Color::Green;
    if (value > 80) bar_color = Color::Red;
    else if (value > 60) bar_color = Color::Yellow;

    std::string bar = std::string(filled, '|') + std::string(width - filled, '.');
    return hbox({
        text(std::format("{:5.1f}% ", value)) | color(Color::GrayDark),
        text(bar) | color(bar_color)
    });
}

void run_tui() {
    auto screen = ScreenInteractive::Fullscreen();

    monitor::MetricsCollector collector;
    pal::ThermalController therm;

    auto metrics = std::make_shared<Metrics>();
    auto freqs = std::make_shared<std::vector<MHz>>();

    auto refresh = std::chrono::seconds(2);

    std::thread refresh_thread([&] {
        while (true) {
            *metrics = collector.collect();
            *freqs = pal::get_current_frequencies();
            std::this_thread::sleep_for(refresh);
        }
    });

    auto renderer = Renderer([&] {
        auto m = *metrics;
        auto f = *freqs;

        std::string profile_name = "auto";
        if (m.cpu_usage > 70) profile_name = "Performance";
        else if (m.gpu_active && m.cpu_usage > 50) profile_name = "Gaming";
        else if (m.cpu_usage > 35) profile_name = "Balanced";
        else profile_name = "Eco";

        auto temp = therm.cpu_temp_celsius();

        auto freq_str = std::format("{} MHz", f.empty() ? 0 : f[0]);

        auto status = window(text(" SpeedCool C++26 "),
            vbox({
                window(text(" Profile "),
                    vbox({
                        text(std::format("Profile: {}", profile_name)),
                        text(std::format("Auto: {}", "ON")),
                    })
                ),
                separator(),
                window(text(" Resources "),
                    vbox({
                        text(std::format("CPU: {:.1f}%", m.cpu_usage)),
                        format_bar(m.cpu_usage),
                        text(std::format("RAM: {:.1f}%", m.ram_percent)),
                        format_bar(m.ram_percent),
                        text(std::format("Temp: {:.1f}°C", temp)),
                        format_bar(temp * 100.0 / 100.0),
                        text(std::format("Freq: {}", freq_str)),
                    })
                ),
                separator(),
                window(text(" System "),
                    vbox({
                        text(std::format("Battery: {:.0f}%", m.battery_level)),
                        text(std::format("AC: {}", m.on_ac ? "connected" : "disconnected")),
                        text(std::format("Load: {:.2f} / {:.2f} / {:.2f}",
                             m.load_1m, m.load_5m, m.load_15m)),
                        text(std::format("GPU active: {}", m.gpu_active ? "yes" : "no")),
                    })
                ),
                separator(),
                text(" [p] Profile  [q] Quit  [r] Refresh ") | color(Color::GrayDark),
            })
        );

        return status | center;
    });

    screen.Loop(renderer);
    refresh_thread.detach();
}

} // namespace speedcool::cli
