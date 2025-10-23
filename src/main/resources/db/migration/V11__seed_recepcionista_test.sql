-- =====================================================
-- Migration V11: Seed Recepcionista de Teste
-- Descrição: Adiciona usuário recepcionista para teste
--            do fluxo de verificação de email e ativação
-- Email: southhenrique@hotmail.com
-- Senha temporária será gerada pelo sistema
-- =====================================================

-- Inserir dados pessoais do recepcionista
INSERT INTO dados_pessoais (id, nome, cpf, telefone, created_at)
SELECT 
    UUID() as id,
    'Henrique South' as nome,
    '11122233344' as cpf,
    '(77) 98765-4321' as telefone,
    NOW() as created_at
WHERE NOT EXISTS (
    SELECT 1 FROM dados_pessoais WHERE cpf = '11122233344'
);

-- Inserir usuário recepcionista
-- Senha temporária: TempSenha@2025 (BCrypt hash with strength 12)
-- O usuário precisará verificar o email e alterar a senha no primeiro acesso
INSERT INTO auth_usuarios (
    uuid, 
    nome, 
    email, 
    cpf, 
    senha_hash, 
    telefone, 
    tipo, 
    ativo, 
    email_verificado,
    senha_temporaria,
    criado_em
)
SELECT 
    UUID() as uuid,
    'Henrique South' as nome,
    'southhenrique@hotmail.com' as email,
    '11122233344' as cpf,
    '$2a$12$KQq8p.4vJ8rFmL9EQqE3JeM5x7EV1bLqYwZxNwRgJ8sHxE5N8QjVe' as senha_hash,
    '(77) 98765-4321' as telefone,
    'RECEPCIONISTA' as tipo,
    FALSE as ativo,
    FALSE as email_verificado,
    TRUE as senha_temporaria,
    NOW() as criado_em
WHERE NOT EXISTS (
    SELECT 1 FROM auth_usuarios WHERE email = 'southhenrique@hotmail.com'
);

-- Comentário: Este usuário está com ativo=FALSE e email_verificado=FALSE
-- O fluxo esperado é:
-- 1. Admin cria o usuário via endpoint (já feito nesta migration)
-- 2. Sistema envia email de verificação
-- 3. Usuário clica no link de verificação (ativa email_verificado=TRUE e ativo=TRUE)
-- 4. Usuário faz primeiro login com senha temporária
-- 5. Sistema força alteração de senha no primeiro acesso
