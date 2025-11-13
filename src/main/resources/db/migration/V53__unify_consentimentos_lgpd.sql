-- Migration V53: Unificar tabelas de consentimentos LGPD em uma única tabela polimórfica
-- Estratégia: Tabela única com discriminador de tipo

-- Criar nova tabela unificada
CREATE TABLE consentimentos_lgpd (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid VARCHAR(36) NOT NULL UNIQUE COMMENT 'UUID único do consentimento',
    
    -- Discriminador polimórfico
    tipo_entidade VARCHAR(50) NOT NULL COMMENT 'USUARIO, PROFISSIONAL, PACIENTE, etc',
    entidade_id BIGINT NOT NULL COMMENT 'ID da entidade (auth_usuarios, profissionais, pacientes)',
    
    -- Informações do Consentimento
    versao_termo VARCHAR(50) NOT NULL COMMENT 'Versão do termo de consentimento (ex: v1.0, v2.0)',
    escopo TEXT NULL COMMENT 'Escopo do consentimento: prontuario, fotos, dados_pessoais, uso_sistema, etc',
    concorda BOOLEAN NOT NULL COMMENT 'True se concordou, False se revogou',
    
    -- Dados de Auditoria do Consentimento
    data_consentimento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Quando o consentimento foi dado/revogado',
    ip_origem VARCHAR(45) NULL COMMENT 'IP de onde o consentimento foi registrado',
    user_agent TEXT NULL COMMENT 'Browser/app usado',
    
    -- Quem registrou o consentimento
    registrado_por_id BIGINT NULL COMMENT 'FK para auth_usuarios que registrou',
    
    -- Metadados adicionais
    metadata JSON NULL COMMENT 'Dados extras: anexo do termo assinado, observações, etc',
    
    -- Auditoria padrão
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by_id BIGINT NULL COMMENT 'FK para auth_usuarios que criou',
    
    -- Índices para performance
    INDEX idx_consentimentos_tipo_entidade (tipo_entidade, entidade_id),
    INDEX idx_consentimentos_data (data_consentimento),
    INDEX idx_consentimentos_versao (versao_termo),
    INDEX idx_consentimentos_uuid (uuid),
    
    -- Foreign Keys
    CONSTRAINT fk_consentimentos_registrado_por FOREIGN KEY (registrado_por_id) 
        REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_consentimentos_created_by FOREIGN KEY (created_by_id) 
        REFERENCES auth_usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Registro unificado e imutável de consentimentos LGPD para todas as entidades do sistema';

-- Migrar dados da tabela de profissionais
INSERT INTO consentimentos_lgpd (
    uuid,
    tipo_entidade,
    entidade_id,
    versao_termo,
    escopo,
    concorda,
    data_consentimento,
    ip_origem,
    user_agent,
    registrado_por_id,
    metadata,
    created_at,
    created_by_id
)
SELECT 
    UUID(),
    'PROFISSIONAL' as tipo_entidade,
    profissional_id as entidade_id,
    versao_termo,
    escopo,
    concorda,
    data_consentimento,
    ip_origem,
    user_agent,
    registrado_por as registrado_por_id,
    metadata,
    COALESCE(created_at, data_consentimento) as created_at,
    created_by as created_by_id
FROM consentimentos_lgpd_profissionais;

-- Migrar dados da tabela de usuários
INSERT INTO consentimentos_lgpd (
    uuid,
    tipo_entidade,
    entidade_id,
    versao_termo,
    escopo,
    concorda,
    data_consentimento,
    ip_origem,
    user_agent,
    registrado_por_id,
    metadata,
    created_at,
    created_by_id
)
SELECT 
    UUID(),
    'USUARIO' as tipo_entidade,
    usuario_id as entidade_id,
    versao_termo,
    escopo,
    concorda,
    data_consentimento,
    ip_origem,
    user_agent,
    registrado_por as registrado_por_id,
    metadata,
    COALESCE(created_at, data_consentimento) as created_at,
    created_by as created_by_id
FROM consentimentos_lgpd_usuarios;

-- Renomear tabelas antigas (manter por segurança antes de deletar)
RENAME TABLE consentimentos_lgpd_profissionais TO _old_consentimentos_lgpd_profissionais;
RENAME TABLE consentimentos_lgpd_usuarios TO _old_consentimentos_lgpd_usuarios;

-- Adicionar comentário explicativo
ALTER TABLE _old_consentimentos_lgpd_profissionais 
COMMENT='DEPRECATED - Migrado para consentimentos_lgpd. Pode ser removido após validação.';

ALTER TABLE _old_consentimentos_lgpd_usuarios 
COMMENT='DEPRECATED - Migrado para consentimentos_lgpd. Pode ser removido após validação.';
