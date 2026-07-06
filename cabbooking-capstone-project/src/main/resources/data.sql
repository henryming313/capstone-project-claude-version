-- Seed data, safe to re-run: INSERT IGNORE skips rows that would violate
-- a unique constraint (username / phone / license_plate), so restarting
-- the app repeatedly will not fail on duplicate seed rows.

-- Passwords (BCrypt hashes below) in plaintext for testing:
--   admin  / admin123
--   rider1 / rider123
--   driver1/ driver123

INSERT IGNORE INTO users (username, password, role, status, phone)
VALUES ('admin', '$2b$10$BWwxQUS.3WeQPywVxycyZeqZpMQwlOLswZZs9pRdV/gmH8tQZtpqi', 'ADMIN', 'ACTIVE', '+358400000001');

INSERT IGNORE INTO users (username, password, role, status, phone)
VALUES ('rider1', '$2b$10$BmARsjizbpRjRutHzB/XZ.OgORYUax7W305QFaGsTtlYKH5ejRYr.', 'RIDER', 'ACTIVE', '+358400000002');

INSERT IGNORE INTO users (username, password, role, status, phone)
VALUES ('driver1', '$2b$10$EgmSxXx39Oc8cAeV2Hq9..u60fVxntR02RrISa7Kc/Sdo/9S3dPWG', 'DRIVER', 'ACTIVE', '+358400000003');

INSERT IGNORE INTO cabs (license_plate, model, status)
VALUES ('KOK-101', 'Toyota Corolla', 'AVAILABLE');

INSERT IGNORE INTO cabs (license_plate, model, status)
VALUES ('KOK-102', 'Skoda Octavia', 'AVAILABLE');

-- Assign driver1 to KOK-101 so the seeded driver can accept trips immediately.
INSERT IGNORE INTO driver_cab_assignment (driver_id, cab_id, assigned_at)
SELECT u.id, c.id, NOW()
FROM users u, cabs c
WHERE u.username = 'driver1' AND c.license_plate = 'KOK-101'
  AND NOT EXISTS (
    SELECT 1 FROM driver_cab_assignment dca
    WHERE dca.driver_id = u.id AND dca.cab_id = c.id
  );
