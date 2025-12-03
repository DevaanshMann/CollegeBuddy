-- Set devaanshmann@cpp.edu as ADMIN
UPDATE users
SET role = 'ADMIN'
WHERE email = 'devaanshmann@cpp.edu';

-- Verify the change
SELECT id, email, role, status, campus_domain
FROM users
WHERE email = 'devaanshmann@cpp.edu';
