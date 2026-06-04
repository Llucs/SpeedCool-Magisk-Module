#!/bin/bash
set -euo pipefail

VERSION="${1:-latest}"
PREFIX="${2:-/usr/local}"
CONFIG_DIR="/etc/speedcool"
SERVICE_DIR="/etc/systemd/system"

echo "SpeedCool C++26 Installer"
echo "========================"

if [[ $EUID -ne 0 ]]; then
    echo "This script must be run as root" >&2
    exit 1
fi

echo "Installing SpeedCool v${VERSION} to ${PREFIX}/bin..."

mkdir -p "${PREFIX}/bin"
mkdir -p "${CONFIG_DIR}"
mkdir -p "/var/log"

cp speedcool "${PREFIX}/bin/speedcool"
chmod +x "${PREFIX}/bin/speedcool"

if [[ -f config/speedcool.toml ]]; then
    cp config/speedcool.toml "${CONFIG_DIR}/speedcool.toml"
fi

cat > "${SERVICE_DIR}/speedcool.service" << 'EOF'
[Unit]
Description=SpeedCool C++26 Adaptive Optimizer
Documentation=https://github.com/user/speedcool-cpp
After=network.target

[Service]
Type=simple
ExecStart=/usr/local/bin/speedcool
Restart=on-failure
RestartSec=10
Environment=SPEEDCOOL_CONFIG=/etc/speedcool/speedcool.toml

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable speedcool.service
systemctl start speedcool.service

echo "SpeedCool installed and running!"
echo "Use: speedcool status"
echo "Use: systemctl status speedcool"
