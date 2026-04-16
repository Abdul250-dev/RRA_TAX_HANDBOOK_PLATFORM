param(
    [switch]$EnableBootstrapAdmin
)

$ErrorActionPreference = "Stop"
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
. (Join-Path $scriptPath "Import-BackendEnv.ps1")
[void](Import-BackendEnv)

if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
    throw "JWT_SECRET is not set. Populate apps/backend/.env from apps/backend/.env.example before starting the backend."
}

if ($EnableBootstrapAdmin) {
    if ([string]::IsNullOrWhiteSpace($env:BOOTSTRAP_ADMIN_USERNAME) -or [string]::IsNullOrWhiteSpace($env:BOOTSTRAP_ADMIN_PASSWORD)) {
        throw "Bootstrap admin startup requested, but BOOTSTRAP_ADMIN_USERNAME or BOOTSTRAP_ADMIN_PASSWORD is missing."
    }

    $env:BOOTSTRAP_ADMIN_ENABLED = "true"
}

Write-Host "Starting backend with local environment configuration..."
Write-Host "Active DB URL: $($env:DATABASE_URL)"
Write-Host "Bootstrap admin enabled: $($env:BOOTSTRAP_ADMIN_ENABLED)"

mvn spring-boot:run
