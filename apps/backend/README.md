# Backend

Spring Boot backend for the RRA Tax Handbook platform.

## Structure

- `src/main/java/com/rra/taxhandbook`: domain modules, security, config, and shared utilities
- `src/main/resources`: profile-based application configuration
- `src/test`: backend tests
- `scripts`: operational helper scripts for rollout and environment checks
- `docs`: backend-specific implementation notes carried over from the original service

## Operations Helpers

- `scripts/Check-FlywayAdoption.ps1`
  - runs or explains the preflight checks for onboarding an older PostgreSQL database to Flyway
- `scripts/flyway-adoption-check.sql`
  - the SQL used by the PowerShell helper and safe to run manually with `psql`
- `scripts/Invoke-BackendSmokeTest.ps1`
  - runs a lightweight post-deploy smoke test for health, login, users summary, audit logs, and public content
- `scripts/Start-BackendLocal.ps1`
  - loads `apps/backend/.env`, validates minimum runtime config, and starts the backend locally
- `.env.example`
  - starter template for local runtime configuration before smoke rehearsal or backend startup

For local smoke rehearsal, the backend still needs runtime security configuration before it can start successfully, especially:

- `JWT_SECRET`
- `BOOTSTRAP_ADMIN_ENABLED=true` when using a temporary bootstrap admin
- `BOOTSTRAP_ADMIN_USERNAME`
- `BOOTSTRAP_ADMIN_PASSWORD`
