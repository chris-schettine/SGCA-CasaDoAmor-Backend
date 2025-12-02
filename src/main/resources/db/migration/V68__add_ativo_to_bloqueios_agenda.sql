-- Migration: Add ativo column to bloqueios_agenda table
-- Description: Adds the missing 'ativo' column to track active/inactive schedule blocks

ALTER TABLE bloqueios_agenda 
ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE 
COMMENT 'Indica se o bloqueio está ativo';

-- Create index for better query performance
CREATE INDEX idx_bloqueios_ativo ON bloqueios_agenda(ativo);
