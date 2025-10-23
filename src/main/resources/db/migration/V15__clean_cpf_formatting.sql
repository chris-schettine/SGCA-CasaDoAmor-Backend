-- Migration: V15 - Limpar formatação de CPF
-- Remove pontos e hífens dos CPFs existentes nas tabelas

-- Limpa CPF na tabela auth_usuarios
UPDATE auth_usuarios 
SET cpf = REPLACE(REPLACE(REPLACE(cpf, '.', ''), '-', ''), ' ', '')
WHERE cpf LIKE '%.%' 
   OR cpf LIKE '%-%'
   OR cpf LIKE '% %';

-- Limpa CPF na tabela dados_pessoais (pacientes)
UPDATE dados_pessoais 
SET cpf = REPLACE(REPLACE(REPLACE(cpf, '.', ''), '-', ''), ' ', '')
WHERE cpf LIKE '%.%' 
   OR cpf LIKE '%-%'
   OR cpf LIKE '% %';

-- Comentário: Esta migration garante que todos os CPFs existentes no banco
-- sejam armazenados apenas com números, removendo pontos, hífens e espaços.
