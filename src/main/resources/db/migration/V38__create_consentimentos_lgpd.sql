-- Migration para criar tabelas de consentimentos LGPD
-- Registros imutáveis de consentimento (auditável) conforme LGPD

-- Tabela de consentimentos para PROFISSIONAIS (funcionários e voluntários)
CREATE TABLE consentimentos_lgpd_profissionais (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Relacionamento
    profissional_id BIGINT NOT NULL COMMENT 'FK para profissionais',
    
    -- Informações do Consentimento
    versao_termo VARCHAR(50) NOT NULL COMMENT 'Versão do termo de consentimento (ex: v1.0, v2.0)',
    escopo VARCHAR(255) NULL COMMENT 'Escopo do consentimento: prontuario, fotos, dados_pessoais, etc',
    concorda BOOLEAN NOT NULL COMMENT 'True se concordou, False se revogou',
    
    -- Dados de Auditoria do Consentimento
    data_consentimento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Quando o consentimento foi dado/revogado',
    ip_origem VARCHAR(45) NULL COMMENT 'IP de onde o consentimento foi registrado',
    user_agent TEXT NULL COMMENT 'Browser/app usado',
    
    -- Quem registrou o consentimento
    registrado_por BIGINT NULL COMMENT 'FK para auth_usuarios que registrou',
    
    -- Metadados adicionais
    metadata JSON NULL COMMENT 'Dados extras: anexo do termo assinado, observações, etc',
    
    -- Índices
    INDEX idx_consentimentos_prof_profissional (profissional_id),
    INDEX idx_consentimentos_prof_data (data_consentimento),
    INDEX idx_consentimentos_prof_versao (versao_termo),
    
    -- Foreign Keys
    CONSTRAINT fk_consentimentos_prof_profissional FOREIGN KEY (profissional_id) 
        REFERENCES profissionais(id) ON DELETE CASCADE,
    CONSTRAINT fk_consentimentos_prof_registrado_por FOREIGN KEY (registrado_por) 
        REFERENCES auth_usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Registro imutável de consentimentos LGPD de profissionais';

-- Tabela de consentimentos para USUÁRIOS DO SISTEMA (auth_usuarios)
CREATE TABLE consentimentos_lgpd_usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Relacionamento
    usuario_id BIGINT NOT NULL COMMENT 'FK para auth_usuarios',
    
    -- Informações do Consentimento
    versao_termo VARCHAR(50) NOT NULL COMMENT 'Versão do termo de consentimento',
    escopo VARCHAR(255) NULL COMMENT 'Escopo do consentimento: uso_sistema, dados_pessoais, logs, etc',
    concorda BOOLEAN NOT NULL COMMENT 'True se concordou, False se revogou',
    
    -- Dados de Auditoria do Consentimento
    data_consentimento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Quando o consentimento foi dado/revogado',
    ip_origem VARCHAR(45) NULL COMMENT 'IP de onde o consentimento foi registrado',
    user_agent TEXT NULL COMMENT 'Browser/app usado',
    
    -- Quem registrou (pode ser o próprio usuário ou um admin)
    registrado_por BIGINT NULL COMMENT 'FK para auth_usuarios que registrou',
    
    -- Metadados adicionais
    metadata JSON NULL COMMENT 'Dados extras: documento assinado, motivo da revogação, etc',
    
    -- Índices
    INDEX idx_consentimentos_user_usuario (usuario_id),
    INDEX idx_consentimentos_user_data (data_consentimento),
    INDEX idx_consentimentos_user_versao (versao_termo),
    
    -- Foreign Keys
    CONSTRAINT fk_consentimentos_user_usuario FOREIGN KEY (usuario_id) 
        REFERENCES auth_usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_consentimentos_user_registrado_por FOREIGN KEY (registrado_por) 
        REFERENCES auth_usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Registro imutável de consentimentos LGPD de usuários do sistema';
