-- Rename timestamp columns in historico_paciente to match BaseEntity naming convention
ALTER TABLE historico_paciente
CHANGE COLUMN criado_em created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE historico_paciente
CHANGE COLUMN atualizado_em updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
