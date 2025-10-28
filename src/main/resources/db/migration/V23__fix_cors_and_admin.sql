-- Migration V23: Correção CORS e senha admin
-- 
-- Esta migration garante que o admin tenha as credenciais corretas
-- após todas as tentativas anteriores.
--
-- Credenciais do Admin:
-- CPF: 99999999999
-- Senha: Admin@123
--
-- Hash gerado com: bcrypt.hashpw('Admin@123'.encode('utf-8'), bcrypt.gensalt(rounds=12, prefix=b'2a'))
-- Formato: $2a$ (compatível com Java BCryptPasswordEncoder)

-- Reseta contadores de tentativas falhas e bloqueio
UPDATE auth_usuarios 
SET tentativas_falhas_de_login = 0,
    locked_until = NULL
WHERE cpf = '99999999999';

-- Limpa histórico de tentativas falhas
DELETE FROM tentativas_login WHERE cpf = '99999999999';

-- Atualiza senha para Admin@123 com hash válido
UPDATE auth_usuarios 
SET senha_hash = '$2a$12$Vka6V6SfOe26P36CAqOZZuTG6demv6NXxltUSF8b1KmIJrRjRgslS',
    senha_temporaria = FALSE,
    ultima_alteracao_senha_em = CURRENT_TIMESTAMP,
    tentativas_falhas_de_login = 0,
    locked_until = NULL
WHERE cpf = '99999999999' 
  AND tipo = 'ADMINISTRADOR';
