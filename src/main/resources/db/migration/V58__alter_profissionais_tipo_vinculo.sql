-- V56: Modificar tabela profissionais
-- Remove campo tipo_vinculo antigo e adiciona FK para tipos_vinculo
-- Modifica CPF para VARCHAR e adiciona backup

-- Adicionar coluna de backup do tipo_vinculo antigo
ALTER TABLE profissionais ADD COLUMN tipo_vinculo_backup VARCHAR(255) NULL COMMENT 'Backup do tipo de vínculo anterior';

-- Copiar dados atuais para backup
UPDATE profissionais SET tipo_vinculo_backup = tipo_vinculo WHERE tipo_vinculo IS NOT NULL;

-- Remover coluna tipo_vinculo antiga
ALTER TABLE profissionais DROP COLUMN tipo_vinculo;

-- Adicionar nova coluna tipo_vinculo_id como FK
ALTER TABLE profissionais ADD COLUMN tipo_vinculo_id BIGINT NULL COMMENT 'FK para tipos_vinculo';

-- Adicionar FK constraint
ALTER TABLE profissionais 
    ADD CONSTRAINT fk_profissionais_tipo_vinculo 
    FOREIGN KEY (tipo_vinculo_id) 
    REFERENCES tipos_vinculo(id);

-- Criar índice para performance
CREATE INDEX idx_profissionais_tipo_vinculo ON profissionais(tipo_vinculo_id);

-- Migrar dados do backup para a nova estrutura
-- FUNCIONARIO -> id 1 (PADRAO) ou id 2 (CLT)
-- VOLUNTARIO -> id 5 (VOL)
UPDATE profissionais 
SET tipo_vinculo_id = CASE 
    WHEN tipo_vinculo_backup = 'FUNCIONARIO' THEN 2  -- CLT
    WHEN tipo_vinculo_backup = 'VOLUNTARIO' THEN 5   -- VOL
    ELSE 1  -- PADRAO como fallback
END
WHERE tipo_vinculo_backup IS NOT NULL;

-- Modificar coluna cpf_criptografado para VARCHAR (descriptografado)
-- Primeiro, adicionar nova coluna cpf como VARCHAR
ALTER TABLE profissionais ADD COLUMN cpf VARCHAR(11) NULL COMMENT 'CPF do profissional (11 dígitos)';

-- Criar índice único no CPF
CREATE UNIQUE INDEX idx_profissionais_cpf ON profissionais(cpf);

-- Nota: A descriptografia do CPF deve ser feita via aplicação Java
-- Este script apenas prepara a estrutura da tabela
