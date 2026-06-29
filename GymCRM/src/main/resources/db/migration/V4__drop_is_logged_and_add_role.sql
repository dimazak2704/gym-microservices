ALTER TABLE users DROP COLUMN is_logged;

ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'TRAINEE';

UPDATE users u
SET role = 'TRAINER'
WHERE EXISTS (SELECT 1 FROM trainers t WHERE t.user_id = u.id);

UPDATE users u
SET role = 'TRAINEE'
WHERE EXISTS (SELECT 1 FROM trainees t WHERE t.user_id = u.id);