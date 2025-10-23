-- ===========================================
--  REMOVE CAMPOS RG E ORGAO_EMISSOR DE auth_usuarios_dados_pessoais
--  Versão: 1.0
--  Data: 2025-10-23
--  Descrição: Remove campos rg e orgao_emissor que não são mais necessários
-- ===========================================

ALTER TABLE auth_usuarios_dados_pessoais
DROP COLUMN rg;

ALTER TABLE auth_usuarios_dados_pessoais
DROP COLUMN orgao_emissor;
