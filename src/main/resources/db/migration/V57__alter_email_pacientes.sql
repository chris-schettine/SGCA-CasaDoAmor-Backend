
UPDATE pacientes
SET email = CONCAT('email_', id, '@default.com')
WHERE email IS NULL OR email = '';

ALTER TABLE pacientes
  MODIFY email VARCHAR(255) NOT NULL;
