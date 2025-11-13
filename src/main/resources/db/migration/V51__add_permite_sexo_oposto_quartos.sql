-- Migration V51: Adicionar campo permite_sexo_oposto e ala ISOLAMENTO em quartos
-- Permite gerenciar quartos que aceitam pacientes de sexo oposto (4 camas debilitados, 7 camas com acompanhantes)

-- 1. Adicionar campo permite_sexo_oposto
ALTER TABLE quartos
ADD COLUMN permite_sexo_oposto TINYINT(1) NOT NULL DEFAULT 0
COMMENT 'Permite pacientes de sexo oposto: TRUE para quartos de 4 camas (debilitados) e 7 camas (acompanhantes)';

-- 2. Atualizar quartos existentes de 4 camas para permitir sexo oposto (pacientes debilitados)
UPDATE quartos
SET permite_sexo_oposto = TRUE
WHERE capacidade_total = 4;

-- 3. Atualizar quartos existentes de 7 camas para permitir sexo oposto (acompanhantes)
UPDATE quartos
SET permite_sexo_oposto = TRUE
WHERE capacidade_total = 7;

-- 4. Modificar enum ala para incluir ISOLAMENTO
-- MySQL não suporta ALTER ENUM diretamente, então precisamos recriar a constraint
ALTER TABLE quartos MODIFY COLUMN ala ENUM('FEMININA', 'MASCULINA', 'MISTA', 'ISOLAMENTO') NOT NULL;

-- Comentários para documentação
ALTER TABLE quartos MODIFY COLUMN ala ENUM('FEMININA', 'MASCULINA', 'MISTA', 'ISOLAMENTO') NOT NULL
COMMENT 'Ala do quarto: FEMININA, MASCULINA, MISTA (sem restrição), ISOLAMENTO (sem restrição)';

-- Adicionar índice para otimizar consultas por permite_sexo_oposto
CREATE INDEX idx_quartos_permite_sexo_oposto ON quartos(permite_sexo_oposto);
