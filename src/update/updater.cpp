#include "update/updater.h"
#include "common/logging.h"
#include <fstream>
#include <filesystem>

namespace fs = std::filesystem;

namespace speedcool::update {

Updater::Updater(std::string repo)
    : repo_(std::move(repo)) {}

auto Updater::current_version() const -> std::string {
    return SPEEDCOOL_VERSION;
}

auto Updater::check() -> Result<std::optional<Release>> {
    auto url = std::format("https://api.github.com/repos/{}/releases/latest", repo_);

    log_info("Checking for updates: {}", url);

    std::string cmd = std::format("curl -sL '{}' -o /tmp/speedcool_latest.json", url);
    int ret = std::system(cmd.c_str());
    if (ret != 0) {
        return std::unexpected(err(Err::UpdateCheckFailed, "Failed to fetch release info"));
    }

    std::ifstream ifs("/tmp/speedcool_latest.json");
    if (!ifs) {
        return std::unexpected(err(Err::UpdateCheckFailed, "Cannot read release info"));
    }

    std::string json((std::istreambuf_iterator<char>(ifs)), {});
    ifs.close();

    auto tag_start = json.find("\"tag_name\":\"");
    if (tag_start == std::string::npos) {
        return std::nullopt;
    }
    tag_start += 12;
    auto tag_end = json.find("\"", tag_start);
    if (tag_end == std::string::npos) {
        return std::nullopt;
    }
    std::string tag = json.substr(tag_start, tag_end - tag_start);

    if (tag == "v" + current_version()) {
        log_info("Already at latest version: {}", tag);
        return std::optional<Release>{};
    }

    Release r;
    r.tag = tag;
    r.url = std::format("https://github.com/{}/releases/tag/{}", repo_, tag);

#ifdef __linux_aarch64__
    r.asset_name = "speedcool-linux-arm64.tar.gz";
#elif __linux__
    r.asset_name = "speedcool-linux-amd64.tar.gz";
#elif _WIN32
    r.asset_name = "speedcool-windows-amd64.zip";
#endif

    auto asset_search = std::format("\"name\":\"{}\"", r.asset_name);
    auto asset_pos = json.find(asset_search);
    if (asset_pos != std::string::npos) {
        auto size_pos = json.find("\"size\":", asset_pos);
        if (size_pos != std::string::npos) {
            size_pos += 7;
            auto size_end = json.find_first_of(",}", size_pos);
            if (size_end != std::string::npos) {
                r.size = std::stoll(json.substr(size_pos, size_end - size_pos));
            }
        }
    }

    log_info("Update available: {} ({} bytes)", r.tag, r.size);
    return r;
}

auto Updater::apply(const Release& release) -> Result<void> {
    log_info("Applying update: {}", release.tag);

    auto download_url = std::format(
        "https://github.com/{}/releases/download/{}/{}",
        repo_, release.tag, release.asset_name);

    std::string cmd = std::format(
        "curl -sL '{}' -o /tmp/{}_update", download_url, "speedcool");
    int ret = std::system(cmd.c_str());
    if (ret != 0) {
        return std::unexpected(err(Err::UpdateApplyFailed, "Download failed"));
    }

    std::string extract_cmd;
#ifdef __linux__
    extract_cmd = "tar xzf /tmp/speedcool_update -C /tmp/speedcool_update_dir && "
                  "cp /tmp/speedcool_update_dir/speedcool /usr/local/bin/speedcool && "
                  "chmod +x /usr/local/bin/speedcool";
#elif _WIN32
    extract_cmd = "powershell -Command \"Expand-Archive -Path /tmp/speedcool_update -DestinationPath /tmp/speedcool_update_dir; "
                  "Copy-Item /tmp/speedcool_update_dir/speedcool.exe $env:ProgramFiles\\SpeedCool\\\"";
#endif

    ret = std::system(extract_cmd.c_str());
    if (ret != 0) {
        return std::unexpected(err(Err::UpdateApplyFailed, "Extraction/install failed"));
    }

    log_info("Update applied: {}", release.tag);
    return {};
}

} // namespace speedcool::update
