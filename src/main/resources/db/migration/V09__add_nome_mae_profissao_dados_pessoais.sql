-- =====================================================
-- Adiciona campos nome_mae e profissao em dados_pessoais
-- V09__add_nome_mae_profissao_dados_pessoais.sql
-- =====================================================

ALTER TABLE dados_pessoais 
ADD COLUMN nome_mae VARCHAR(255) NULL AFTER nome;

ALTER TABLE dados_pessoais
ADD COLUMN profissao VARCHAR(100) NULL AFTER email;
