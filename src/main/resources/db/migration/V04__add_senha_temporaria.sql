-- Migration V04: Adiciona campo senha_temporaria na tabela auth_usuarios
-- Criado em: 16/10/2025
-- Descrição: Adiciona flag para controle de senha temporária (primeiro acesso)

ALTER TABLE auth_usuarios 
ADD COLUMN senha_temporaria BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Indica se usuário possui senha temporária que deve ser alterada';

-- Atualiza comentário da coluna
ALTER TABLE auth_usuarios 
MODIFY COLUMN senha_temporaria BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Flag para forçar troca de senha no primeiro acesso';
