-- V18__enable_2fa_for_existing_users.sql
-- Habilita 2FA para todos os usuários existentes no sistema

-- Insere registros de 2FA habilitado para todos os usuários que ainda não têm
INSERT INTO autenticacao_2fa (usuario_id, habilitado, criado_em, atualizado_em)
SELECT 
    id,
    TRUE,
    NOW(),
    NOW()
FROM auth_usuarios
WHERE id NOT IN (SELECT usuario_id FROM autenticacao_2fa)
  AND ativo = TRUE
  AND deletado_em IS NULL;

-- Habilita 2FA para qualquer registro existente que esteja desabilitado
UPDATE autenticacao_2fa
SET 
    habilitado = TRUE,
    atualizado_em = NOW()
WHERE habilitado = FALSE;
