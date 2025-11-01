-- Limpa dados existentes na tabela para evitar problemas de FK
DELETE FROM acompanhantes;

-- Adiciona coluna parentesco
ALTER TABLE acompanhantes
  ADD COLUMN parentesco VARCHAR(100);

-- Adiciona coluna paciente_id como NOT NULL
ALTER TABLE acompanhantes
  ADD COLUMN paciente_id CHAR(36) NOT NULL;

-- Cria a constraint de foreign key
ALTER TABLE acompanhantes
  ADD CONSTRAINT fk_acompanhantes_paciente
    FOREIGN KEY (paciente_id)
    REFERENCES pacientes(id)
    ON DELETE CASCADE;
