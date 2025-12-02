-- Migration V58: Adiciona coluna para armazenamento de foto de paciente
-- Criado em: 30/11/2025
-- Descrição: Adiciona suporte para caminho da imagem no bucket S3/storage

ALTER TABLE pacientes 
    ADD COLUMN caminho_da_imagem_no_bucket VARCHAR(255) NULL COMMENT 'Caminho da foto do paciente no storage/bucket';

-- Índice para buscar pacientes com foto
CREATE INDEX idx_pacientes_foto ON pacientes(caminho_da_imagem_no_bucket);
