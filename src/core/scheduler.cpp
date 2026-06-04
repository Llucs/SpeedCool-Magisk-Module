#include "core/scheduler.h"
#include "common/logging.h"
#include <thread>

namespace speedcool::core {

Scheduler::Scheduler(u32 interval_sec)
    : interval_sec_(interval_sec) {}

Scheduler::~Scheduler() {
    stop();
}

auto Scheduler::start(Task task) -> void {
    if (running_.exchange(true)) return;

    worker_ = std::thread([this, task = std::move(task)]() {
        log_info("Scheduler started (interval: {}s)", interval_sec_);
        while (running_) {
            task();
            std::this_thread::sleep_for(std::chrono::seconds(interval_sec_));
        }
    });
}

auto Scheduler::stop() -> void {
    if (!running_.exchange(false)) return;
    if (worker_.joinable()) worker_.join();
    log_info("Scheduler stopped");
}

auto Scheduler::set_interval(u32 interval_sec) -> void {
    interval_sec_ = interval_sec;
}

auto Scheduler::is_running() const -> bool {
    return running_.load();
}

} // namespace speedcool::core
