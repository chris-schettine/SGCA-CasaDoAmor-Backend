-- Migration V21: Corrigir hash da senha do administrador
-- 
-- Contexto: As migrations V19 e V20 tentaram corrigir a senha do admin com CPF 00000000000,
-- mas o usuário admin correto no sistema usa CPF 99999999999.
-- Esta migration corrige o hash da senha para o admin correto.
--
-- Credenciais do Admin:
-- CPF: 99999999999
-- Senha: Admin@123
-- Hash: BCrypt com strength 12 ($2b$ format)
--
-- O hash foi gerado usando: bcrypt.hashpw('Admin@123'.encode('utf-8'), bcrypt.gensalt(rounds=12))
-- e testado para garantir compatibilidade com BCryptPasswordEncoder do Spring Security
-- IMPORTANTE: Usando formato $2a$ (compatível com Java) em vez de $2b$ (Python)

UPDATE auth_usuarios 
SET senha_hash = '$2a$12$kYK9GWl0W3y7hcJhGW.d0eVxmM9EJkWYT3220KbMU7Iva2QcNFM0e',
    senha_temporaria = FALSE,
    ultima_alteracao_senha_em = CURRENT_TIMESTAMP
WHERE cpf = '99999999999' 
  AND tipo = 'ADMINISTRADOR';
