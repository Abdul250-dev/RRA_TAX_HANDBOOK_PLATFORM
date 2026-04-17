alter table users add column if not exists first_name varchar(255);
alter table users add column if not exists last_name varchar(255);
alter table users add column if not exists username varchar(100);
alter table users add column if not exists is_active boolean not null default true;
alter table users add column if not exists is_locked boolean not null default false;
alter table users add column if not exists failed_login_attempts integer not null default 0;
alter table users add column if not exists last_login_at timestamp with time zone;
alter table users add column if not exists phone_number varchar(50);
alter table users add column if not exists department varchar(100);
alter table users add column if not exists position varchar(100);
alter table users add column if not exists updated_at timestamp with time zone;
alter table users add column if not exists deleted_at timestamp with time zone;
alter table users add column if not exists created_by bigint;
alter table users add column if not exists updated_by bigint;

update users
set first_name = coalesce(nullif(split_part(full_name, ' ', 1), ''), full_name),
    last_name = coalesce(nullif(trim(substring(full_name from length(split_part(full_name, ' ', 1)) + 1)), ''), 'User')
where first_name is null
   or last_name is null;

update users
set username = lower(split_part(email, '@', 1))
where username is null;

update users
set status = 'PENDING'
where status = 'INVITED';

update users
set status = 'DEACTIVATED'
where status = 'REMOVED';

update users
set is_active = case when status = 'ACTIVE' then true else false end,
    is_locked = coalesce(is_locked, false),
    failed_login_attempts = coalesce(failed_login_attempts, 0),
    deleted_at = case when status = 'DEACTIVATED' then coalesce(deleted_at, created_at) else deleted_at end;

alter table users alter column employee_id type varchar(100);
alter table users alter column first_name set not null;
alter table users alter column last_name set not null;
alter table users alter column status type varchar(50);

alter table users drop column if exists full_name;

alter table users add constraint uq_users_username unique (username);
alter table users add constraint chk_users_status check (status in ('PENDING', 'ACTIVE', 'SUSPENDED', 'DEACTIVATED'));
