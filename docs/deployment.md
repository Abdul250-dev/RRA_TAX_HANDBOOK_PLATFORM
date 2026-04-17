# Deployment

Use the root `docker-compose.yml` for local orchestration and `infra` for environment-specific assets.

## Backend Profiles

The backend uses Spring profiles to separate local and production behavior:

- `dev`
  - default local profile
  - provides local PostgreSQL defaults when env vars are not supplied
  - uses Flyway-managed schema startup
  - keeps `ddl-auto: validate`
  - keeps `sql.init.mode: never`
- `prod`
  - must be activated explicitly with `SPRING_PROFILES_ACTIVE=prod`
  - requires database credentials from environment variables
  - uses `ddl-auto: validate`
  - uses `sql.init.mode: never`
  - uses Flyway-managed schema startup

## Required Production Environment Variables

Provide these values in production:

- `SPRING_PROFILES_ACTIVE=prod`
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
  - `MAIL_HOST`
  - `MAIL_PORT`
  - `APP_MAIL_FROM`
  - `APP_MAIL_INVITE_ACCEPT_URL`
  - `APP_MAIL_RESET_PASSWORD_URL`
  - `APP_MAIL_QUEUE_MAX_ATTEMPTS`
  - `APP_MAIL_QUEUE_RETRY_DELAY_SECONDS`
  - `APP_MAIL_QUEUE_PROCESSING_ENABLED`
  - `APP_MAIL_QUEUE_PROCESSING_FIXED_DELAY_MS`

Optional but commonly needed:

- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`
- `MAIL_SMTP_STARTTLS`
- `APP_MAIL_DELIVERY_MAX_ATTEMPTS`
- `APP_MAIL_DELIVERY_RETRY_DELAY_MS`
- `BOOTSTRAP_ADMIN_ENABLED`
- `BOOTSTRAP_ADMIN_USERNAME`
- `BOOTSTRAP_ADMIN_PASSWORD`
- `APP_SECURITY_RATE_LIMIT_ENABLED`
- `APP_SECURITY_RATE_LIMIT_MAX_REQUESTS`
- `APP_SECURITY_RATE_LIMIT_WINDOW_SECONDS`

## Production Notes

- Production no longer relies on Hibernate schema mutation.
- Production no longer runs SQL initialization automatically.
- Database schema and seed history now live under `apps/backend/src/main/resources/db/migration`.
- Existing environments should baseline or align their schema before first Flyway-managed startup.
- `APP_SECURITY_EXPOSE_SENSITIVE_TOKENS` should remain disabled in production.
- Bootstrap admin access should stay disabled unless there is a short, controlled setup task.
- `APP_MAIL_INVITE_ACCEPT_URL` and `APP_MAIL_RESET_PASSWORD_URL` must point to real deployed frontend routes and must include the `{token}` placeholder.
- Under the `prod` profile, backend startup now fails fast if mail links point to `localhost` or omit the `{token}` placeholder.
- Invite and reset emails now retry SMTP delivery a configurable number of times before the request fails.
- Failed invite/reset deliveries are also persisted in the backend email queue and retried by the scheduled processor.

## Migration Rollout Notes

- New schema changes should be added as versioned Flyway files under `apps/backend/src/main/resources/db/migration`.
- `V1__initial_schema.sql` is the baseline schema snapshot for managed environments.
- `V3__refactor_users_for_admin_provisioning.sql` reshapes the legacy `users` table into the admin-managed account model.
- `V4__drop_employee_directory_snapshot.sql` removes the old employee snapshot table.
- `V6__add_email_notification_queue.sql` adds durable invite/reset delivery persistence for retry processing.
- For an existing database that was previously created by Hibernate, do not point production at Flyway blindly.
- First compare the live schema with the managed migration chain, then baseline or reconcile the database before enabling the new startup path.

## Release Operations

Use [backend-release-runbook.md](./backend-release-runbook.md) for:

- pre-release checks
- migration rollout steps
- post-deploy verification
- rollback decisions
- ownership and escalation during release

Use [flyway-adoption-runbook.md](./flyway-adoption-runbook.md) when:

- onboarding an older environment to Flyway for the first time
- deciding between baselining and schema reconciliation
- validating a database that predates the current migration chain

For a runnable preflight against PostgreSQL, use:

- `.\apps\backend\scripts\Check-FlywayAdoption.ps1`

For a runnable post-deploy backend verification, use:

- `.\apps\backend\scripts\Invoke-BackendSmokeTest.ps1`

For local rehearsal setup, start from:

- `apps/backend/.env.example`
- `.\apps\backend\scripts\Start-BackendLocal.ps1`
