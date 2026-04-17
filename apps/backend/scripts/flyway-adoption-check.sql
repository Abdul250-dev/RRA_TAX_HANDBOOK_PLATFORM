\echo '=== Flyway adoption readiness check ==='

\echo ''
\echo '1) Flyway history table'
select to_regclass('public.flyway_schema_history') as flyway_schema_history;

\echo ''
\echo '2) Current migration files expected by this backend'
select 'V1__initial_schema.sql' as expected_migration
union all select 'V2__seed_employee_directory_snapshot.sql'
union all select 'V3__refactor_users_for_admin_provisioning.sql'
union all select 'V4__drop_employee_directory_snapshot.sql'
union all select 'V5__add_topic_scheduled_publish.sql';

\echo ''
\echo '3) Users table columns'
select column_name, data_type
from information_schema.columns
where table_schema = 'public'
  and table_name = 'users'
order by ordinal_position;

\echo ''
\echo '4) Users status distribution'
select status, count(*) as row_count
from users
group by status
order by status;

\echo ''
\echo '5) Legacy employee snapshot table'
select to_regclass('public.employee_directory_snapshot') as employee_directory_snapshot;

\echo ''
\echo '6) Scheduled publish column on content_topics'
select column_name
from information_schema.columns
where table_schema = 'public'
  and table_name = 'content_topics'
  and column_name = 'scheduled_publish_at';

\echo ''
\echo '7) Flyway-applied versions if history exists'
select installed_rank, version, description, success
from flyway_schema_history
order by installed_rank;
