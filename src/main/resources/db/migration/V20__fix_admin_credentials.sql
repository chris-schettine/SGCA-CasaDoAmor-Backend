-- V20__fix_admin_credentials.sql
-- Corrige credenciais do administrador e garante 2FA habilitado

-- Remove registros antigos do admin se existirem
DELETE FROM autenticacao_2fa WHERE usuario_id IN (SELECT id FROM auth_usuarios WHERE cpf = '00000000000');
DELETE FROM tentativas_login WHERE cpf = '00000000000';
DELETE FROM auth_usuarios WHERE cpf = '00000000000';

-- Recria admin com senha Admin@123 e configurações corretas
-- Hash BCrypt (strength 12) para: Admin@123
INSERT INTO auth_usuarios (uuid, nome, email, cpf, senha_hash, telefone, tipo, ativo, email_verificado, tentativas_falhas_de_login, senha_temporaria, criado_em)
VALUES (
    UUID(),
    'Admin Sistema',
    'admin@casadoamor.com',
    '00000000000',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOem06OEkJQmFn.SzYIOEL.KdDX7aSEV6',
    '(77) 99999-9999',
    'ADMINISTRADOR',
    TRUE,
    TRUE,
    0,
    FALSE,
    NOW()
);

-- Habilita 2FA para o admin
INSERT INTO autenticacao_2fa (usuario_id, habilitado, criado_em, atualizado_em)
SELECT id, TRUE, NOW(), NOW()
FROM auth_usuarios
WHERE cpf = '00000000000';
