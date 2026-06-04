#pragma once

#include "common/types.h"
#include "common/error.h"
#include <string>
#include <optional>

namespace speedcool::update {

struct Release {
    std::string tag;
    std::string url;
    std::string checksum;
    std::string asset_name;
    i64 size{};
};

class Updater {
public:
    explicit Updater(std::string repo);

    auto check() -> Result<std::optional<Release>>;
    auto apply(const Release& release) -> Result<void>;
    auto current_version() const -> std::string;

private:
    std::string repo_;
};

} // namespace speedcool::update
