-- Migration V54: Adiciona colunas para armazenamento de fotos de profissionais
-- Criado em: 13/11/2025
-- Descrição: Adiciona suporte para upload de fotos de perfil dos profissionais (mesmo padrão de auth_usuarios)

ALTER TABLE profissionais 
    ADD COLUMN foto_url VARCHAR(500) NULL COMMENT 'URL completa para acessar a foto',
    ADD COLUMN foto_path VARCHAR(255) NULL COMMENT 'Caminho relativo do arquivo no storage',
    ADD COLUMN foto_atualizada_em TIMESTAMP NULL COMMENT 'Data/hora da última atualização da foto';

-- Índice para buscar profissionais com foto
CREATE INDEX idx_profissionais_foto ON profissionais(foto_path);
