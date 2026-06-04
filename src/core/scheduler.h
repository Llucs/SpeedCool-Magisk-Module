#pragma once

#include "common/types.h"
#include <functional>
#include <thread>
#include <atomic>
#include <chrono>

namespace speedcool::core {

class Scheduler {
public:
    using Task = std::function<void()>;

    explicit Scheduler(u32 interval_sec);
    ~Scheduler();

    auto start(Task task) -> void;
    auto stop() -> void;
    auto set_interval(u32 interval_sec) -> void;
    auto is_running() const -> bool;

private:
    u32 interval_sec_;
    std::atomic<bool> running_{false};
    std::thread worker_;
};

} // namespace speedcool::core
