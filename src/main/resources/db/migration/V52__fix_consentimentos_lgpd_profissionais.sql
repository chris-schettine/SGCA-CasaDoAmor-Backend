-- Migration V52: Corrigir estrutura da tabela consentimentos_lgpd_profissionais
-- Garantir compatibilidade com a entidade JPA

-- Modificar coluna escopo para ser NULLABLE conforme definição original
ALTER TABLE consentimentos_lgpd_profissionais
MODIFY COLUMN escopo VARCHAR(255) NULL COMMENT 'Escopo do consentimento: prontuario, fotos, dados_pessoais, etc';

-- Garantir que created_at tenha valor padrão
ALTER TABLE consentimentos_lgpd_profissionais
MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Adicionar created_by se não existir (auditoria)
-- Verifica primeiro se a coluna não existe
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'consentimentos_lgpd_profissionais'
  AND COLUMN_NAME = 'created_by';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE consentimentos_lgpd_profissionais ADD COLUMN created_by BIGINT NULL COMMENT \"FK para auth_usuarios que criou o registro\"',
    'SELECT \"Column created_by already exists\" AS info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Adicionar FK para created_by se não existir
SET @fk_exists = 0;
SELECT COUNT(*) INTO @fk_exists
FROM information_schema.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'consentimentos_lgpd_profissionais'
  AND CONSTRAINT_NAME = 'fk_consentimentos_prof_created_by';

SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE consentimentos_lgpd_profissionais ADD CONSTRAINT fk_consentimentos_prof_created_by FOREIGN KEY (created_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL',
    'SELECT \"FK fk_consentimentos_prof_created_by already exists\" AS info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
