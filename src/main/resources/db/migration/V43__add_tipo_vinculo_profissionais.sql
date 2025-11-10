-- Adiciona coluna tipo_vinculo na tabela profissionais
-- Diferencia Funcionários (contratados) de Voluntários

ALTER TABLE profissionais 
ADD COLUMN tipo_vinculo VARCHAR(20) NOT NULL DEFAULT 'FUNCIONARIO' 
AFTER uuid;

-- Atualiza constraint para garantir valores válidos
ALTER TABLE profissionais 
ADD CONSTRAINT chk_tipo_vinculo 
CHECK (tipo_vinculo IN ('FUNCIONARIO', 'VOLUNTARIO'));
