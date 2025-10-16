-- Migration V05: Adiciona colunas para armazenamento de fotos
-- Criado em: 16/10/2025
-- Descrição: Adiciona suporte para upload de fotos de perfil dos usuários

ALTER TABLE auth_usuarios 
    ADD COLUMN foto_url VARCHAR(500) NULL COMMENT 'URL completa para acessar a foto',
    ADD COLUMN foto_path VARCHAR(255) NULL COMMENT 'Caminho relativo do arquivo no storage',
    ADD COLUMN foto_atualizada_em TIMESTAMP NULL COMMENT 'Data/hora da última atualização da foto';

-- Índice para buscar usuários com foto
CREATE INDEX idx_auth_usuarios_foto ON auth_usuarios(foto_path);
