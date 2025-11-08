ALTER TABLE pacientes
ADD COLUMN dado_social_id CHAR(36),
ADD COLUMN politica_privacidade_id CHAR(36),
ADD CONSTRAINT fk_paciente_dado_social
  FOREIGN KEY (dado_social_id) REFERENCES dados_sociais(id)
  ON DELETE SET NULL ON UPDATE CASCADE,
ADD CONSTRAINT fk_paciente_politica_privacidade
  FOREIGN KEY (politica_privacidade_id) REFERENCES politica_privacidade(id)
  ON DELETE SET NULL ON UPDATE CASCADE;
