CREATE TABLE obituarios (
  id CHAR(36) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,

  data_hora_obito DATETIME(6) NULL,
  local_obito VARCHAR(255) NULL,
  causa_obito VARCHAR(255) NULL,
  numero_declaracao_obito VARCHAR(255) NULL,
  observacoes_obito TEXT NULL,
  responsavel_comunicacao_obito VARCHAR(255) NULL,

  PRIMARY KEY (id)
);

ALTER TABLE pacientes
ADD COLUMN obituario_id CHAR(36) NULL,
ADD CONSTRAINT fk_paciente_obituario
  FOREIGN KEY (obituario_id)
  REFERENCES obituarios(id)
  ON DELETE SET NULL;

ALTER TABLE pacientes
ADD COLUMN status ENUM('ATIVO', 'INATIVO', 'FALECIDO') DEFAULT 'ATIVO';

UPDATE pacientes
SET status = 'ATIVO'
WHERE obituario_id IS NULL;

UPDATE pacientes
SET status = 'FALECIDO'
WHERE obituario_id IS NOT NULL;

ALTER TABLE pacientes
MODIFY COLUMN status ENUM('ATIVO', 'INATIVO', 'FALECIDO') NOT NULL;
