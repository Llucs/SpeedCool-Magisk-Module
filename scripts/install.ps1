param(
    [string]$Version = "latest",
    [string]$InstallDir = "$env:ProgramFiles\SpeedCool"
)

Write-Host "SpeedCool C++26 Installer for Windows" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "This script must be run as Administrator" -ForegroundColor Red
    exit 1
}

Write-Host "Installing SpeedCool v$Version to $InstallDir..."

New-Item -ItemType Directory -Force -Path $InstallDir | Out-Null
New-Item -ItemType Directory -Force -Path "$env:ALLUSERSPROFILE\SpeedCool" | Out-Null

Copy-Item "speedcool.exe" -Destination "$InstallDir\speedcool.exe" -Force

if (Test-Path "config\speedcool.toml") {
    Copy-Item "config\speedcool.toml" -Destination "$env:ALLUSERSPROFILE\SpeedCool\speedcool.toml" -Force
}

New-Service -Name "SpeedCool" `
    -BinaryPathName "$InstallDir\speedcool.exe" `
    -DisplayName "SpeedCool C++26 Optimizer" `
    -Description "Cross-platform adaptive system optimizer" `
    -StartupType Automatic

Start-Service -Name "SpeedCool"

[Environment]::SetEnvironmentVariable("SPEEDCOOL_CONFIG", "$env:ALLUSERSPROFILE\SpeedCool\speedcool.toml", "Machine")

Write-Host "SpeedCool installed and running!" -ForegroundColor Green
Write-Host "Use: speedcool status" -ForegroundColor Yellow
