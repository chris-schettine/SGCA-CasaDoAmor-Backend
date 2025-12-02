-- Migration V34: Ensure RG uniqueness
-- Note: This constraint already exists from V01 (rg VARCHAR(10) UNIQUE)
-- This migration is kept as a no-op for Flyway version tracking only
SELECT 'V34 - RG unique constraint already exists from V01' AS migration_note;
