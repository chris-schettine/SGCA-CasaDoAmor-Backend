CREATE TABLE informacoes_hospitalares (
  id CHAR(36) NOT NULL PRIMARY KEY,
  nome_hospital_referencia VARCHAR(255),
  medico_responsavel VARCHAR(150),
  setor_ala VARCHAR(100),
  data_internacao DATE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE pacientes
ADD COLUMN informacao_hospitalar_id CHAR(36),
ADD CONSTRAINT fk_paciente_informacao_hospitalar
  FOREIGN KEY (informacao_hospitalar_id)
  REFERENCES informacoes_hospitalares(id)
  ON DELETE SET NULL
  ON UPDATE CASCADE;
