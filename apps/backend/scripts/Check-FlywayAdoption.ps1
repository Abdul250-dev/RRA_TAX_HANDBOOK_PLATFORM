param(
    [string]$DatabaseUrl = $env:DATABASE_URL,
    [string]$DatabaseUsername = $env:DATABASE_USERNAME,
    [string]$DatabasePassword = $env:DATABASE_PASSWORD,
    [switch]$ExplainOnly
)

$ErrorActionPreference = "Stop"
$PSNativeCommandUseErrorActionPreference = $false

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$sqlFile = Join-Path $scriptPath "flyway-adoption-check.sql"
. (Join-Path $scriptPath "Import-BackendEnv.ps1")

$loadedEnv = Import-BackendEnv

if ([string]::IsNullOrWhiteSpace($DatabaseUrl)) {
    $DatabaseUrl = $env:DATABASE_URL
}
if ([string]::IsNullOrWhiteSpace($DatabaseUsername)) {
    $DatabaseUsername = $env:DATABASE_USERNAME
}
if ([string]::IsNullOrWhiteSpace($DatabasePassword)) {
    $DatabasePassword = $env:DATABASE_PASSWORD
}

function Find-Psql {
    $command = Get-Command psql -ErrorAction SilentlyContinue
    if ($null -ne $command) {
        return $command.Source
    }

    $candidateRoots = @(
        "C:\Program Files\PostgreSQL",
        "C:\Program Files (x86)\PostgreSQL"
    )

    foreach ($root in $candidateRoots) {
        if (Test-Path $root) {
            $match = Get-ChildItem $root -Recurse -Filter psql.exe -ErrorAction SilentlyContinue |
                Sort-Object FullName |
                Select-Object -First 1
            if ($null -ne $match) {
                return $match.FullName
            }
        }
    }

    return $null
}

function Invoke-Psql {
    param(
        [string]$Executable,
        [string[]]$Arguments
    )

    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $Executable
    $startInfo.Arguments = (($Arguments | ForEach-Object {
        if ($_ -match '\s') {
            '"' + $_.Replace('"', '\"') + '"'
        } else {
            $_
        }
    }) -join ' ')
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    [void]$process.Start()

    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    return @{
        ExitCode = $process.ExitCode
        StdOut = $stdout
        StdErr = $stderr
    }
}

if (-not (Test-Path $sqlFile)) {
    throw "Missing SQL file at $sqlFile"
}

Write-Host "Flyway adoption readiness check"
Write-Host ""

if ($loadedEnv) {
    Write-Host "Loaded local environment values from apps/backend/.env"
    Write-Host ""
}

if ([string]::IsNullOrWhiteSpace($DatabaseUrl)) {
    Write-Warning "DATABASE_URL is not set. The checker can still show guidance, but it cannot connect automatically."
}

if (-not $ExplainOnly) {
    $psql = Find-Psql

    if ($null -eq $psql) {
        Write-Warning "psql was not found on PATH or common PostgreSQL install locations. Install PostgreSQL CLI tools or run the SQL file manually."
    } elseif ([string]::IsNullOrWhiteSpace($DatabaseUrl)) {
        Write-Warning "Skipping automatic execution because DATABASE_URL is missing."
    } else {
        Write-Host "Running readiness SQL with psql at: $psql"

        $previousPassword = $env:PGPASSWORD
        try {
            if (-not [string]::IsNullOrWhiteSpace($DatabasePassword)) {
                $env:PGPASSWORD = $DatabasePassword
            }

            $args = @(
                $DatabaseUrl,
                "-f", $sqlFile
            )

            if (-not [string]::IsNullOrWhiteSpace($DatabaseUsername)) {
                $args += @("-U", $DatabaseUsername)
            }

            $result = Invoke-Psql -Executable $psql -Arguments $args
            if ($result.ExitCode -ne 0) {
                $joinedOutput = (($result.StdOut, $result.StdErr) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) -join [Environment]::NewLine

                if ($joinedOutput -match "password authentication failed") {
                    throw "psql authentication failed. Check DATABASE_USERNAME and DATABASE_PASSWORD for the target PostgreSQL environment."
                }

                if ($joinedOutput -match "database .* does not exist") {
                    throw "psql could not find the target database. Check DATABASE_URL before running the Flyway adoption preflight."
                }

                throw "psql exited with code $($result.ExitCode)`n$joinedOutput"
            }

            if (-not [string]::IsNullOrWhiteSpace($result.StdOut)) {
                Write-Output $result.StdOut
            }
        } finally {
            $env:PGPASSWORD = $previousPassword
        }
    }
}

Write-Host ""
Write-Host "Readiness interpretation checklist:"
Write-Host "1. If flyway_schema_history is null, this environment predates Flyway adoption."
Write-Host "2. If employee_directory_snapshot still exists, V4 has not been applied yet."
Write-Host "3. If users still contains legacy-only columns or statuses (INVITED/REMOVED), reconcile before baselining."
Write-Host "4. If scheduled_publish_at is missing from content_topics, V5 has not been applied yet."
Write-Host "5. Baseline only when the live schema truly matches the intended migration state."
Write-Host ""
Write-Host "Local setup tip: copy apps/backend/.env.example to apps/backend/.env and fill in the real database and JWT settings before rehearsal."
Write-Host ""
Write-Host "If you need the manual procedure, see docs/flyway-adoption-runbook.md"
