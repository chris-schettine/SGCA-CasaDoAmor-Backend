-- Migration V03: Autenticação 2FA (Two-Factor Authentication)
-- Criado em: 16/10/2025
-- Descrição: Adiciona suporte para autenticação de dois fatores via email

CREATE TABLE autenticacao_2fa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    habilitado BOOLEAN NOT NULL DEFAULT FALSE,
    data_habilitacao DATETIME(6),
    data_desabilitacao DATETIME(6),
    codigo_atual VARCHAR(6),
    expiracao_codigo DATETIME(6),
    tentativas_falhas INT NOT NULL DEFAULT 0,
    bloqueado_ate DATETIME(6),
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6),
    
    CONSTRAINT fk_autenticacao_2fa_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES auth_usuarios(id) 
        ON DELETE CASCADE,
    
    INDEX idx_usuario_habilitado (usuario_id, habilitado),
    INDEX idx_codigo_expiracao (codigo_atual, expiracao_codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comentários das colunas
ALTER TABLE autenticacao_2fa 
    MODIFY COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID único da configuração 2FA',
    MODIFY COLUMN usuario_id BIGINT NOT NULL UNIQUE COMMENT 'ID do usuário (FK para auth_usuarios)',
    MODIFY COLUMN habilitado BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Indica se 2FA está ativo',
    MODIFY COLUMN data_habilitacao DATETIME(6) COMMENT 'Data/hora em que 2FA foi habilitado',
    MODIFY COLUMN data_desabilitacao DATETIME(6) COMMENT 'Data/hora em que 2FA foi desabilitado',
    MODIFY COLUMN codigo_atual VARCHAR(6) COMMENT 'Código de 6 dígitos enviado por email',
    MODIFY COLUMN expiracao_codigo DATETIME(6) COMMENT 'Data/hora de expiração do código (5 minutos)',
    MODIFY COLUMN tentativas_falhas INT NOT NULL DEFAULT 0 COMMENT 'Contador de tentativas falhas',
    MODIFY COLUMN bloqueado_ate DATETIME(6) COMMENT 'Data/hora até quando está bloqueado (15 min após 5 erros)',
    MODIFY COLUMN criado_em DATETIME(6) NOT NULL COMMENT 'Data/hora de criação do registro',
    MODIFY COLUMN atualizado_em DATETIME(6) COMMENT 'Data/hora da última atualização';
