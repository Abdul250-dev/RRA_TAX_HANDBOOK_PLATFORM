param(
    [string]$BaseUrl = "http://localhost:8081",
    [string]$AdminUsername = $env:SMOKE_ADMIN_USERNAME,
    [string]$AdminPassword = $env:SMOKE_ADMIN_PASSWORD,
    [string]$Locale = "EN",
    [string]$TopicSlug = $env:SMOKE_TOPIC_SLUG,
    [switch]$SkipAdminChecks
)

$ErrorActionPreference = "Stop"
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
. (Join-Path $scriptPath "Import-BackendEnv.ps1")
[void](Import-BackendEnv)

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Action
    )

    Write-Host ""
    Write-Host "==> $Name"
    & $Action
}

function Assert-Status {
    param(
        [string]$Name,
        [int]$Expected,
        [int]$Actual
    )

    if ($Actual -ne $Expected) {
        throw "$Name failed. Expected HTTP $Expected but got $Actual."
    }
}

Write-Host "Backend smoke test"
Write-Host "Base URL: $BaseUrl"
Write-Host "Tip: for local rehearsal, make sure the backend was started with JWT_SECRET set and, if needed, BOOTSTRAP_ADMIN_* credentials enabled."

Invoke-Step "Health" {
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl/api/health" -Method GET -UseBasicParsing
    } catch {
        throw "Health check could not reach $BaseUrl. Make sure the backend is running and started with valid DATABASE_* and JWT_SECRET settings."
    }

    Assert-Status -Name "Health" -Expected 200 -Actual $response.StatusCode
    Write-Host "Health check passed."
}

$jwtToken = $null

if (-not $SkipAdminChecks) {
    if ([string]::IsNullOrWhiteSpace($AdminUsername) -or [string]::IsNullOrWhiteSpace($AdminPassword)) {
        throw "Admin smoke checks require SMOKE_ADMIN_USERNAME and SMOKE_ADMIN_PASSWORD, or pass -SkipAdminChecks."
    }

    Invoke-Step "Admin Login" {
        $loginBody = @{
            username = $AdminUsername
            password = $AdminPassword
        } | ConvertTo-Json

        $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -ContentType "application/json" -Body $loginBody

        if ([string]::IsNullOrWhiteSpace($response.token)) {
            throw "Admin login succeeded without returning a token."
        }

        $script:jwtToken = $response.token
        Write-Host "Admin login passed."
    }

    Invoke-Step "Users Summary" {
        $headers = @{ Authorization = "Bearer $jwtToken" }
        $response = Invoke-WebRequest -Uri "$BaseUrl/api/users/summary" -Method GET -Headers $headers -UseBasicParsing
        Assert-Status -Name "Users Summary" -Expected 200 -Actual $response.StatusCode
        Write-Host "Users summary passed."
    }

    Invoke-Step "Audit Logs" {
        $headers = @{ Authorization = "Bearer $jwtToken" }
        $response = Invoke-WebRequest -Uri "$BaseUrl/api/audit-logs" -Method GET -Headers $headers -UseBasicParsing
        Assert-Status -Name "Audit Logs" -Expected 200 -Actual $response.StatusCode
        Write-Host "Audit log access passed."
    }
}

Invoke-Step "Public Sections" {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/public/sections?locale=$Locale" -Method GET -UseBasicParsing
    Assert-Status -Name "Public Sections" -Expected 200 -Actual $response.StatusCode
    Write-Host "Public sections passed."
}

if (-not [string]::IsNullOrWhiteSpace($TopicSlug)) {
    Invoke-Step "Public Topic" {
        $response = Invoke-WebRequest -Uri "$BaseUrl/api/public/topics/$TopicSlug?locale=$Locale" -Method GET -UseBasicParsing
        Assert-Status -Name "Public Topic" -Expected 200 -Actual $response.StatusCode
        Write-Host "Public topic passed."
    }
} else {
    Write-Host ""
    Write-Host "Skipping public topic check because SMOKE_TOPIC_SLUG was not provided."
}

Write-Host ""
Write-Host "Smoke test completed successfully."
