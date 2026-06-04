#pragma once

#include <string>
#include <mutex>
#include <format>

namespace speedcool {

enum class LogLevel { Trace, Debug, Info, Warn, Error };

void set_log_level(LogLevel l);
void set_log_file(const std::string& path);
auto log_level_from_string(const std::string& s) -> LogLevel;
void log_impl(LogLevel l, const std::string& msg);

template<typename... Args>
void log(LogLevel l, std::format_string<Args...> fmt, Args&&... args) {
    log_impl(l, std::format(fmt, std::forward<Args>(args)...));
}

template<typename... Args>
void log_trace(std::format_string<Args...> fmt, Args&&... args) {
    log(LogLevel::Trace, fmt, std::forward<Args>(args)...);
}

template<typename... Args>
void log_debug(std::format_string<Args...> fmt, Args&&... args) {
    log(LogLevel::Debug, fmt, std::forward<Args>(args)...);
}

template<typename... Args>
void log_info(std::format_string<Args...> fmt, Args&&... args) {
    log(LogLevel::Info, fmt, std::forward<Args>(args)...);
}

template<typename... Args>
void log_warn(std::format_string<Args...> fmt, Args&&... args) {
    log(LogLevel::Warn, fmt, std::forward<Args>(args)...);
}

template<typename... Args>
void log_error(std::format_string<Args...> fmt, Args&&... args) {
    log(LogLevel::Error, fmt, std::forward<Args>(args)...);
}

} // namespace speedcool
