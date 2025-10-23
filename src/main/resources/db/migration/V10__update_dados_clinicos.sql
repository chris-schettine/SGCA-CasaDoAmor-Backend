-- Adicionar novos campos à tabela dados_clinicos

-- Remover coluna antiga tipo_sonda (será substituída pelos campos mais específicos)
ALTER TABLE dados_clinicos DROP COLUMN tipo_sonda;

-- Campos para tipos de sonda nasal/oral
ALTER TABLE dados_clinicos ADD COLUMN tipo_sonda_nasal VARCHAR(50);

-- Campos para tipos de sonda cirúrgica
ALTER TABLE dados_clinicos ADD COLUMN tipo_sonda_cirurgica VARCHAR(50);

-- Campo para diagnóstico expandido (modificar para TEXT)
ALTER TABLE dados_clinicos MODIFY COLUMN diagnostico TEXT;

-- Campos para tratamento (não precisa modificar, já é VARCHAR(255))
ALTER TABLE dados_clinicos ADD COLUMN tratamento_outro_descricao TEXT;

-- Campos para condição de chegada
ALTER TABLE dados_clinicos ADD COLUMN condicao_chegada VARCHAR(50);

-- Campos para sonda vesical
ALTER TABLE dados_clinicos ADD COLUMN tipo_sonda_vesical VARCHAR(50);

-- Descrição para outras sondas
ALTER TABLE dados_clinicos ADD COLUMN sonda_outra_descricao TEXT;

-- Campo para oxigenoterapia
ALTER TABLE dados_clinicos ADD COLUMN usa_oxigenoterapia BOOLEAN NOT NULL DEFAULT FALSE;

-- Índices para melhorar performance de consultas
CREATE INDEX idx_dados_clinicos_paciente ON dados_clinicos(paciente_id);
CREATE INDEX idx_dados_clinicos_tratamento ON dados_clinicos(tratamento);
CREATE INDEX idx_dados_clinicos_condicao_chegada ON dados_clinicos(condicao_chegada);
