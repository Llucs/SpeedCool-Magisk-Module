#pragma once

#include "common/types.h"
#include "common/error.h"
#include <string>

namespace speedcool::profile {

class ProfileManager {
public:
    explicit ProfileManager(const ProfileConfig profiles[5]);

    auto apply(ProfileType type) -> Result<void>;
    auto current() const -> ProfileType;
    auto get_config(ProfileType type) const -> const ProfileConfig&;
    auto set_config(ProfileType type, const ProfileConfig& cfg) -> void;
    auto name(ProfileType type) const -> std::string;

    static auto profile_name(ProfileType type) -> std::string;

private:
    ProfileConfig profiles_[5];
    ProfileType current_{ProfileType::Balanced};

    auto apply_linux(const ProfileConfig& p) -> Result<void>;
    auto apply_windows(const ProfileConfig& p) -> Result<void>;
};

} // namespace speedcool::profile
