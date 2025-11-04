CREATE TABLE historico_paciente (
  id CHAR(36) PRIMARY KEY,
  paciente_id CHAR(36) NOT NULL,
  acompanhante_id CHAR(36) NULL,
  descricao TEXT NOT NULL,
  data_registro TIMESTAMP NOT NULL,
  criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deletado_em TIMESTAMP NULL,
  
  CONSTRAINT fk_historico_paciente_paciente
    FOREIGN KEY (paciente_id)
    REFERENCES pacientes(id)
    ON DELETE CASCADE,

  CONSTRAINT fk_historico_paciente_acompanhante
    FOREIGN KEY (acompanhante_id)
    REFERENCES acompanhantes(id)
    ON DELETE SET NULL
);
