-- =====================================================
-- Adiciona campo nome_mae em dados_pessoais
-- V09__add_nome_mae_profissao_dados_pessoais.sql
-- =====================================================

-- Adiciona nome_mae após o campo nome
ALTER TABLE dados_pessoais 
ADD COLUMN nome_mae VARCHAR(255) NULL AFTER nome;
