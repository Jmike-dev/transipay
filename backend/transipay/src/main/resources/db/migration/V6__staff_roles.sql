UPDATE staff SET role = UPPER(role);
UPDATE staff SET role = 'STAFF' WHERE role NOT IN ('ADMIN', 'STAFF');

ALTER TABLE staff ALTER COLUMN role SET DEFAULT 'STAFF';
ALTER TABLE staff ADD CONSTRAINT staff_role_check CHECK (role IN ('ADMIN', 'STAFF'));