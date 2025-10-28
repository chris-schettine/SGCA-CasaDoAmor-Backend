-- Migration V21: Corrigir hash da senha do administrador
-- Senha: Admin@123
-- Hash BCrypt com strength 12 ($2b$)

UPDATE auth_usuarios 
SET senha_hash = '$2b$12$YMsMFwsmGtdt5pH.LgMTh.KSnXlGjcsds2rE98c8tEmg7TqplFqpm',
    senha_temporaria = FALSE,
    ultima_alteracao_senha_em = CURRENT_TIMESTAMP
WHERE cpf = '99999999999' 
  AND tipo = 'ADMINISTRADOR';
