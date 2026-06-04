#pragma once

#include <system_error>
#include <string>
#include <expected>

namespace speedcool {

enum class Err {
    Success = 0,
    NotFound,
    PermissionDenied,
    IoError,
    ParseError,
    NotSupported,
    ConflictDetected,
    UpdateCheckFailed,
    UpdateApplyFailed,
    InvalidArgument,
    ServiceNotRunning,
    AlreadyRunning,
    ConfigError,
    PlatformError
};

class SpeedCoolError : public std::system_error {
public:
    SpeedCoolError(Err e, std::string msg = "")
        : std::system_error(std::error_code(static_cast<int>(e), error_category())),
          err_(e), msg_(std::move(msg)) {}

    Err code() const noexcept { return err_; }
    const std::string& message() const noexcept { return msg_; }

private:
    Err err_;
    std::string msg_;

    static const std::error_category& error_category() {
        struct SpeedCoolCategory : std::error_category {
            const char* name() const noexcept override { return "speedcool"; }
            std::string message(int ev) const override {
                switch (static_cast<Err>(ev)) {
                    case Err::Success: return "success";
                    case Err::NotFound: return "not found";
                    case Err::PermissionDenied: return "permission denied";
                    case Err::IoError: return "I/O error";
                    case Err::ParseError: return "parse error";
                    case Err::NotSupported: return "not supported on this platform";
                    case Err::ConflictDetected: return "conflict detected";
                    case Err::UpdateCheckFailed: return "update check failed";
                    case Err::UpdateApplyFailed: return "update apply failed";
                    case Err::InvalidArgument: return "invalid argument";
                    case Err::ServiceNotRunning: return "service not running";
                    case Err::AlreadyRunning: return "already running";
                    case Err::ConfigError: return "configuration error";
                    case Err::PlatformError: return "platform error";
                    default: return "unknown";
                }
            }
        };
        static SpeedCoolCategory cat;
        return cat;
    }
};

template<typename T>
using Result = std::expected<T, SpeedCoolError>;

inline auto err(Err e, std::string msg = "") -> SpeedCoolError {
    return SpeedCoolError(e, std::move(msg));
}

} // namespace speedcool
