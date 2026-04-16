function Import-BackendEnv {
    param(
        [string]$EnvFile = (Join-Path (Split-Path -Parent $PSScriptRoot) ".env"),
        [switch]$OverwriteExisting
    )

    if (-not (Test-Path $EnvFile)) {
        return $false
    }

    Get-Content $EnvFile | ForEach-Object {
        $line = $_.Trim()
        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith("#")) {
            return
        }

        $parts = $line -split "=", 2
        if ($parts.Count -ne 2) {
            return
        }

        $key = $parts[0].Trim()
        $value = $parts[1]

        if ($OverwriteExisting -or [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($key, "Process"))) {
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
        }
    }

    return $true
}
