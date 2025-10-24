-- =====================================================
-- Migration V16: Criar tabela de registros profissionais
-- =====================================================
-- Tabela para armazenar registros profissionais (CRM, COREN, CRO, etc.)
-- Esta tabela é IMUTÁVEL - uma vez criado, o registro não pode ser editado

CREATE TABLE auth_usuarios_registros_profissionais (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    tipo_profissional ENUM('DENTISTA', 'ENFERMEIRO', 'FISIOTERAPEUTA', 'MEDICO', 'NUTRICIONISTA') NOT NULL,
    numero_registro VARCHAR(50) NOT NULL COMMENT 'Número do registro profissional (CRM, COREN, CRO, CREFITO, CRN)',
    rqe VARCHAR(50) NULL COMMENT 'Registro de Qualificação de Especialista (opcional, usado por médicos e dentistas)',
    
    -- Auditoria (apenas criação, sem atualização pois é imutável)
    criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
    criado_por BIGINT NULL COMMENT 'ID do usuário admin que criou o registro',
    
    -- Constraints
    CONSTRAINT fk_registro_profissional_usuario 
        FOREIGN KEY (usuario_id) REFERENCES auth_usuarios(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_registro_profissional_criador 
        FOREIGN KEY (criado_por) REFERENCES auth_usuarios(id) 
        ON DELETE SET NULL,
    
    -- Garantir que cada usuário tenha apenas um registro profissional
    CONSTRAINT uk_registro_profissional_usuario 
        UNIQUE (usuario_id),
    
    -- Garantir que o número de registro seja único por tipo
    CONSTRAINT uk_registro_profissional_numero_tipo 
        UNIQUE (tipo_profissional, numero_registro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registros profissionais imutáveis (CRM, COREN, CRO, CREFITO, CRN)';

-- Índices para melhorar performance de consultas
CREATE INDEX idx_registro_profissional_tipo ON auth_usuarios_registros_profissionais(tipo_profissional);
CREATE INDEX idx_registro_profissional_numero ON auth_usuarios_registros_profissionais(numero_registro);

-- Comentários explicativos
-- DENTISTA: CRO (Conselho Regional de Odontologia) + RQE opcional
-- ENFERMEIRO: COREN (Conselho Regional de Enfermagem)
-- FISIOTERAPEUTA: CREFITO (Conselho Regional de Fisioterapia e Terapia Ocupacional)
-- MEDICO: CRM (Conselho Regional de Medicina) + RQE opcional
-- NUTRICIONISTA: CRN (Conselho Regional de Nutricionistas)
