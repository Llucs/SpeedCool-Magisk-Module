param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("start", "stop", "restart", "status")]
    [string]$Action
)

$ServiceName = "SpeedCool"

switch ($Action) {
    "start" {
        Start-Service -Name $ServiceName
        Write-Host "SpeedCool started"
    }
    "stop" {
        Stop-Service -Name $ServiceName
        Write-Host "SpeedCool stopped"
    }
    "restart" {
        Restart-Service -Name $ServiceName
        Write-Host "SpeedCool restarted"
    }
    "status" {
        Get-Service -Name $ServiceName
    }
}
