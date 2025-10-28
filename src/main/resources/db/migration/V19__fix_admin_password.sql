-- V19__fix_admin_password.sql
-- Atualiza a senha do administrador para um hash conhecido funcionando

-- Hash BCrypt (strength 12) para a senha: Admin@123
-- Gerado e testado manualmente
UPDATE auth_usuarios 
SET senha_hash = '$2a$12$qbIC3NA71l2f5cGMhRrIKO.53MWP5o7I12gdYfqI.Mt/CG6MlvBCG'
WHERE cpf = '00000000000';
