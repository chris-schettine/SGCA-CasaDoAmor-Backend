UPDATE dados_pessoais
SET data_nascimento = '2000-01-01'
WHERE data_nascimento IS NULL;

ALTER TABLE dados_pessoais
MODIFY COLUMN data_nascimento DATE NOT NULL;
