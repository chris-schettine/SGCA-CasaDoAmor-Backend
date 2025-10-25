-- =====================================================
-- Migration V17: Criar tabela de rate limiting para 2FA
-- =====================================================
-- Controla o envio excessivo de códigos 2FA
-- Previne abuso e spam de emails

CREATE TABLE autenticacao_2fa_rate_limit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    ultimo_envio DATETIME NOT NULL,
    tentativas_ultimos_15min INT NOT NULL DEFAULT 0 COMMENT 'Máximo 3 em 15 minutos',
    tentativas_ultima_hora INT NOT NULL DEFAULT 0 COMMENT 'Máximo 5 por hora',
    tentativas_hoje INT NOT NULL DEFAULT 0 COMMENT 'Máximo 10 por dia',
    bloqueado_ate DATETIME NULL COMMENT 'Até quando está bloqueado',
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    
    -- Índices
    INDEX idx_2fa_rate_limit_usuario (usuario_id),
    
    -- Garante um registro por usuário
    UNIQUE KEY uk_2fa_rate_limit_usuario (usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Controle de rate limiting para envio de códigos 2FA - previne abuso';

-- Comentário explicativo
-- Limites implementados:
-- - Máximo 3 códigos em 15 minutos
-- - Máximo 5 códigos por hora
-- - Máximo 10 códigos por dia
-- - Mínimo 60 segundos entre envios
