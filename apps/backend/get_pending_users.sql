-- Query to get all pending users and their verification tokens
-- Run this with: psql -h localhost -U collegebuddy -d collegebuddy -f get_pending_users.sql

SELECT
    u.id AS user_id,
    u.email,
    u.campus_domain,
    u.status,
    u.created_at AS user_created_at,
    vt.token AS verification_token,
    vt.expires_at AS token_expires_at,
    CASE
        WHEN vt.expires_at < NOW() THEN 'EXPIRED'
        ELSE 'VALID'
    END AS token_status
FROM users u
LEFT JOIN verification_tokens vt ON u.id = vt.user_id
WHERE u.status = 'PENDING_VERIFICATION'
ORDER BY u.created_at DESC;
