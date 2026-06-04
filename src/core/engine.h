#pragma once

#include "common/types.h"
#include "common/error.h"
#include <array>

namespace speedcool::engine {

struct Decision {
    ProfileType profile{ProfileType::Balanced};
    f64 confidence{};
};

auto decide(const Metrics& m, const DaemonConfig& cfg) -> Decision;
auto calculate_next_interval(const Metrics& m, u32 base_interval_sec) -> u32;
auto check_conflicts(const std::vector<std::string>& known_modules) -> bool;

class Predictor {
public:
    Predictor(f64 learning_rate = 0.01);
    auto predict(const Metrics& m) -> f64;
    auto train(const Metrics& m, f64 actual_score) -> void;
    auto weights() const -> const std::array<f64, 4>&;
    auto set_weights(const std::array<f64, 4>& w) -> void;

private:
    std::array<f64, 4> weights_{};
    f64 learning_rate_;
};

} // namespace speedcool::engine
