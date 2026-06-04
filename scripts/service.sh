#!/bin/bash
set -euo pipefail

SERVICE_NAME="speedcool"

case "${1:-help}" in
    start)
        systemctl start "${SERVICE_NAME}"
        echo "${SERVICE_NAME} started"
        ;;
    stop)
        systemctl stop "${SERVICE_NAME}"
        echo "${SERVICE_NAME} stopped"
        ;;
    restart)
        systemctl restart "${SERVICE_NAME}"
        echo "${SERVICE_NAME} restarted"
        ;;
    status)
        systemctl status "${SERVICE_NAME}"
        ;;
    enable)
        systemctl enable "${SERVICE_NAME}"
        echo "${SERVICE_NAME} enabled"
        ;;
    disable)
        systemctl disable "${SERVICE_NAME}"
        echo "${SERVICE_NAME} disabled"
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|enable|disable}"
        exit 1
        ;;
esac
