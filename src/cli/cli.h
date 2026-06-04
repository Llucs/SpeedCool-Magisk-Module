#pragma once

namespace speedcool::cli {

class Cli {
public:
    auto run(int argc, char* argv[]) -> int;

private:
    auto cmd_status() -> int;
    auto cmd_perf(int argc, char* argv[]) -> int;
    auto cmd_monitor() -> int;
    auto cmd_history() -> int;
    auto cmd_update(int argc, char* argv[]) -> int;
    auto print_help() -> int;
};

} // namespace speedcool::cli
