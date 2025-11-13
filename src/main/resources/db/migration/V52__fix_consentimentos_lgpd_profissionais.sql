-- Migration V52: Corrigir estrutura das tabelas de consentimentos LGPD
-- Garantir compatibilidade com as entidades JPA

-- ======================================
-- PROFISSIONAIS
-- ======================================

-- Modificar coluna escopo para ser NULLABLE conforme definição original
ALTER TABLE consentimentos_lgpd_profissionais
MODIFY COLUMN escopo VARCHAR(255) NULL COMMENT 'Escopo do consentimento: prontuario, fotos, dados_pessoais, etc';

-- Adicionar created_at na tabela de profissionais se não existir
SET @col_exists_ca = 0;
SELECT COUNT(*) INTO @col_exists_ca 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'consentimentos_lgpd_profissionais'
  AND COLUMN_NAME = 'created_at';

SET @sql_ca = IF(@col_exists_ca = 0,
    'ALTER TABLE consentimentos_lgpd_profissionais ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT \"Data de criação do registro\"',
    'SELECT \"Column created_at already exists in profissionais table\" AS info');

PREPARE stmt_ca FROM @sql_ca;
EXECUTE stmt_ca;
DEALLOCATE PREPARE stmt_ca;

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

-- ======================================
-- USUÁRIOS
-- ======================================

-- Adicionar created_at na tabela de usuários se não existir
SET @col_exists_user = 0;
SELECT COUNT(*) INTO @col_exists_user 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'consentimentos_lgpd_usuarios'
  AND COLUMN_NAME = 'created_at';

SET @sql_user = IF(@col_exists_user = 0,
    'ALTER TABLE consentimentos_lgpd_usuarios ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT \"Data de criação do registro\"',
    'SELECT \"Column created_at already exists in usuarios table\" AS info');

PREPARE stmt_user FROM @sql_user;
EXECUTE stmt_user;
DEALLOCATE PREPARE stmt_user;

-- Adicionar created_by na tabela de usuários se não existir
SET @col_exists_user_by = 0;
SELECT COUNT(*) INTO @col_exists_user_by 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'consentimentos_lgpd_usuarios'
  AND COLUMN_NAME = 'created_by';

SET @sql_user_by = IF(@col_exists_user_by = 0,
    'ALTER TABLE consentimentos_lgpd_usuarios ADD COLUMN created_by BIGINT NULL COMMENT \"FK para auth_usuarios que criou o registro\"',
    'SELECT \"Column created_by already exists in usuarios table\" AS info');

PREPARE stmt_user_by FROM @sql_user_by;
EXECUTE stmt_user_by;
DEALLOCATE PREPARE stmt_user_by;

-- Adicionar FK para created_by na tabela de usuários se não existir
SET @fk_exists_user = 0;
SELECT COUNT(*) INTO @fk_exists_user
FROM information_schema.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'consentimentos_lgpd_usuarios'
  AND CONSTRAINT_NAME = 'fk_consentimentos_user_created_by';

SET @sql_fk_user = IF(@fk_exists_user = 0,
    'ALTER TABLE consentimentos_lgpd_usuarios ADD CONSTRAINT fk_consentimentos_user_created_by FOREIGN KEY (created_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL',
    'SELECT \"FK fk_consentimentos_user_created_by already exists\" AS info');

PREPARE stmt_fk_user FROM @sql_fk_user;
EXECUTE stmt_fk_user;
DEALLOCATE PREPARE stmt_fk_user;

-- Modificar coluna escopo na tabela de usuários para ser TEXT (mais flexível)
ALTER TABLE consentimentos_lgpd_usuarios
MODIFY COLUMN escopo TEXT NULL COMMENT 'Escopo do consentimento: uso_sistema, dados_pessoais, logs, etc';
