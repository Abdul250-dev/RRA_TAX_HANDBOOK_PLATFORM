# Database Schema

PostgreSQL is the intended primary datastore for the backend service.

## Migration Source Of Truth

The backend now uses Flyway-managed migrations for runtime schema history in managed environments.

- migration files live in `apps/backend/src/main/resources/db/migration`
- `V1__initial_schema.sql` is the baseline schema snapshot
- `V2__seed_employee_directory_snapshot.sql` moves employee seed data into migration history
- `V3__refactor_users_for_admin_provisioning.sql` reshapes `users` for admin-created accounts
- `V4__drop_employee_directory_snapshot.sql` removes the old employee snapshot table

## Environment Behavior

- `dev`
  - default profile
  - connects to local PostgreSQL by default
  - uses Flyway startup plus `ddl-auto: validate`
- `prod`
  - must be activated explicitly
  - requires environment-provided database credentials
  - uses Flyway startup plus `ddl-auto: validate`
- `test`
  - still uses H2 with `create-drop`
  - does not currently run Flyway
  - this is a temporary compatibility choice to keep the Spring Boot 4 test suite stable

## Current Managed Tables

The current baseline migration includes these core tables:

- `audit_logs`
- `roles`
- `users`
- `content_sections`
- `content_section_translations`
- `content_topics`
- `content_topic_translations`
- `content_topic_blocks`
- `content_topic_block_translations`

## Rollout Guidance

- New database changes should be added as new versioned Flyway migrations instead of changing `V1`.
- Existing environments created by Hibernate should be reviewed before first Flyway-managed startup.
- Existing environments that already contain user records should review the `V3` status mapping and full-name split before rollout.
- If a deployed database already matches the baseline closely, use a Flyway baselining strategy before enabling migration enforcement.
- If it does not match, reconcile the schema first instead of letting application startup discover drift unexpectedly.
- Use [flyway-adoption-runbook.md](./flyway-adoption-runbook.md) for the concrete baseline-versus-reconcile procedure and verification steps.
