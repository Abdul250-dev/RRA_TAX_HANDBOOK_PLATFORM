# Security

Security configuration is implemented under `apps/backend/src/main/java/com/rra/taxhandbook/security`.

## Runtime Security Defaults

- bootstrap admin access is opt-in only
- sensitive invite/reset token exposure is disabled by default
- public auth endpoints are rate limited
- JWT signing requires a configured secret
- schema management no longer depends on Hibernate auto-update in deployed environments

## Production Expectations

For production deployments:

- run with `SPRING_PROFILES_ACTIVE=prod`
- provide `JWT_SECRET`
- provide `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`
- keep `APP_SECURITY_EXPOSE_SENSITIVE_TOKENS=false`
- enable bootstrap admin only for tightly controlled setup windows
- keep Flyway migration history intact and review new migration files during release preparation
- provide `APP_MAIL_INVITE_ACCEPT_URL` and `APP_MAIL_RESET_PASSWORD_URL` as real deployed frontend URLs that include the `{token}` placeholder
- review `APP_MAIL_QUEUE_*` settings so durable retry cadence and queue processing fit the production provider and incident-response expectations
- review `APP_MAIL_DELIVERY_MAX_ATTEMPTS` and `APP_MAIL_DELIVERY_RETRY_DELAY_MS` so retry behavior matches the production provider and timeout expectations

## Operational Security Checks

For each release, confirm:

- login still rejects suspended, locked, and deactivated users
- rate limiting is enabled for public auth endpoints unless there is a deliberate reason not to
- bootstrap admin remains disabled unless explicitly needed for a short setup window
- audit log creation still works for user, auth, role, and content mutations
- new migrations do not weaken unique constraints or status enforcement on `users`
- invite and reset emails open the intended deployed frontend routes and do not point to `localhost`

## Security Ownership During Release

For each production release, explicitly assign:

- who watches auth and login behavior
- who validates audit logging after deployment
- who decides whether a security-sensitive regression requires rollback
