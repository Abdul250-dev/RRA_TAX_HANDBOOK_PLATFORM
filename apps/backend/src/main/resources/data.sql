ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_locale VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS source VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS invite_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS invite_expires_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_expires_at TIMESTAMP;

UPDATE users SET preferred_locale = 'EN' WHERE preferred_locale IS NULL;
UPDATE users SET source = 'LOCAL' WHERE source IS NULL;

INSERT INTO employee_directory_snapshot (employee_id, full_name, email, active)
SELECT 'RRA-001', 'Abdul Aziz', 'admin@rra.gov.rw', true
WHERE NOT EXISTS (
    SELECT 1 FROM employee_directory_snapshot WHERE employee_id = 'RRA-001'
);

INSERT INTO employee_directory_snapshot (employee_id, full_name, email, active)
SELECT 'RRA-002', 'Prince NSHUTI', 'content.officer@rra.gov.rw', true
WHERE NOT EXISTS (
    SELECT 1 FROM employee_directory_snapshot WHERE employee_id = 'RRA-002'
);

INSERT INTO employee_directory_snapshot (employee_id, full_name, email, active)
SELECT 'RRA-003', 'Alice Mukamana', 'alice.mukamana@rra.gov.rw', true
WHERE NOT EXISTS (
    SELECT 1 FROM employee_directory_snapshot WHERE employee_id = 'RRA-003'
);

INSERT INTO employee_directory_snapshot (employee_id, full_name, email, active)
SELECT 'RRA-004', 'Eric Nshimiyimana', 'eric.nshimiyimana@rra.gov.rw', true
WHERE NOT EXISTS (
    SELECT 1 FROM employee_directory_snapshot WHERE employee_id = 'RRA-004'
);

INSERT INTO employee_directory_snapshot (employee_id, full_name, email, active)
SELECT 'RRA-005', 'Beatrice Uwase', 'beatrice.uwase@rra.gov.rw', true
WHERE NOT EXISTS (
    SELECT 1 FROM employee_directory_snapshot WHERE employee_id = 'RRA-005'
);

INSERT INTO employee_directory_snapshot (employee_id, full_name, email, active)
SELECT 'RRA-006', 'Patrick Habimana', 'patrick.habimana@rra.gov.rw', true
WHERE NOT EXISTS (
    SELECT 1 FROM employee_directory_snapshot WHERE employee_id = 'RRA-006'
);

INSERT INTO employee_directory_snapshot (employee_id, full_name, email, active)
SELECT 'RRA-007', 'Diane Iradukunda', 'diane.iradukunda@rra.gov.rw', true
WHERE NOT EXISTS (
    SELECT 1 FROM employee_directory_snapshot WHERE employee_id = 'RRA-007'
);

INSERT INTO employee_directory_snapshot (employee_id, full_name, email, active)
SELECT 'RRA-008', 'Jean Claude Niyonzima', 'jeanclaude.niyonzima@rra.gov.rw', true
WHERE NOT EXISTS (
    SELECT 1 FROM employee_directory_snapshot WHERE employee_id = 'RRA-008'
);

INSERT INTO employee_directory_snapshot (employee_id, full_name, email, active)
SELECT 'RRA-009', 'Chantal Uwera', 'chantal.uwera@rra.gov.rw', true
WHERE NOT EXISTS (
    SELECT 1 FROM employee_directory_snapshot WHERE employee_id = 'RRA-009'
);

INSERT INTO employee_directory_snapshot (employee_id, full_name, email, active)
SELECT 'RRA-010', 'Samuel Mugisha', 'samuel.mugisha@rra.gov.rw', true
WHERE NOT EXISTS (
    SELECT 1 FROM employee_directory_snapshot WHERE employee_id = 'RRA-010'
);
