#include "common/logging.h"
#include <print>
#include <fstream>
#include <chrono>
#include <iomanip>
#include <sstream>

namespace speedcool {

namespace {
    LogLevel g_level{LogLevel::Info};
    std::string g_logfile;
    std::mutex g_log_mtx;

    auto level_str(LogLevel l) -> const char* {
        switch (l) {
            case LogLevel::Trace: return "TRACE";
            case LogLevel::Debug: return "DEBUG";
            case LogLevel::Info:  return "INFO";
            case LogLevel::Warn:  return "WARN";
            case LogLevel::Error: return "ERROR";
            default: return "UNKNOWN";
        }
    }

    auto timestamp_str() -> std::string {
        auto now = std::chrono::system_clock::now();
        auto t = std::chrono::system_clock::to_time_t(now);
        std::tm tm{};
#ifdef _WIN32
        localtime_s(&tm, &t);
#else
        localtime_r(&t, &tm);
#endif
        auto ms = std::chrono::duration_cast<std::chrono::milliseconds>(
            now.time_since_epoch()).count() % 1000;
        std::ostringstream oss;
        oss << std::put_time(&tm, "%Y-%m-%d %H:%M:%S") << '.' << std::setfill('0') << std::setw(3) << ms;
        return oss.str();
    }
}

void set_log_level(LogLevel l) { g_level = l; }
void set_log_file(const std::string& path) { g_logfile = path; }

auto log_level_from_string(const std::string& s) -> LogLevel {
    if (s == "trace") return LogLevel::Trace;
    if (s == "debug") return LogLevel::Debug;
    if (s == "info")  return LogLevel::Info;
    if (s == "warn")  return LogLevel::Warn;
    if (s == "error") return LogLevel::Error;
    return LogLevel::Info;
}

void log_impl(LogLevel l, const std::string& msg) {
    if (l < g_level) return;
    std::lock_guard<std::mutex> lock(g_log_mtx);
    auto line = std::format("[{}] [{}] {}\n", timestamp_str(), level_str(l), msg);
    std::print("{}", line);
    if (!g_logfile.empty()) {
        if (std::ofstream ofs{g_logfile, std::ios::app}) {
            ofs << line;
        }
    }
}

} // namespace speedcool
