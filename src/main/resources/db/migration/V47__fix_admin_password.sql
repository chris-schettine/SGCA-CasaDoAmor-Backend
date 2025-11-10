-- V47__fix_admin_password.sql
-- Atualiza a senha do administrador para uma senha conhecida e funcional

-- Atualizar senha do admin para: Admin@123
-- Este hash foi gerado com Python bcrypt (força 12) e testado
-- BCrypt $2b$ é compatível com Spring Security BCryptPasswordEncoder
-- Senha temporária será FALSE para permitir acesso direto
UPDATE auth_usuarios 
SET 
    senha_hash = '$2b$12$NjDTo9ETU9JsQbp0ViXnCeh/Nij08N2q8E/XYOYaZRU2hFxqvGdim',
    senha_temporaria = FALSE,
    tentativas_falhas_de_login = 0,
    locked_until = NULL,
    atualizado_em = NOW()
WHERE cpf = '00000000000';
