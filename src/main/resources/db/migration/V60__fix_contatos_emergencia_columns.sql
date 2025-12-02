-- Migration V60: Corrige nomes de colunas em contatos_emergencia
-- Criado em: 30/11/2025
-- Descrição: Renomeia createdAt/updatedAt para created_at/updated_at (snake_case)

ALTER TABLE contatos_emergencia 
    CHANGE COLUMN createdAt created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHANGE COLUMN updatedAt updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
