#pragma once

#include "common/types.h"
#include <vector>
#include <string>

namespace speedcool::pal {

struct IoController {
    auto set_scheduler(const std::string& dev, const std::string& sched) -> bool;
    auto set_read_ahead(const std::string& dev, KB kb) -> bool;
    auto set_nr_requests(const std::string& dev, u32 n) -> bool;
    auto devices() -> std::vector<BlkDevice>;
    auto apply_profile_scheduler(const std::string& sched) -> bool;
};

} // namespace speedcool::pal
