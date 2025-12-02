-- Migration para criar tabela de agendamentos
-- UC5.7, UC5.8: Agendamento e remarcação/cancelamento de serviços
-- UC3.7: Agendar Atendimento Individual

CREATE TABLE agendamentos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid CHAR(36) UNIQUE NOT NULL,
    
    -- Relacionamentos
    tipo_servico_id BIGINT NOT NULL COMMENT 'FK para tipos_servico',
    paciente_id CHAR(36) NOT NULL COMMENT 'FK para pacientes',
    profissional_id BIGINT NULL COMMENT 'FK para profissionais (pode ser null se não atribuído)',
    acompanhante_id CHAR(36) NULL COMMENT 'FK para acompanhantes (opcional)',
    
    -- Data e Hora do Agendamento
    data_inicio DATETIME NOT NULL COMMENT 'Data e hora de início',
    data_fim DATETIME NOT NULL COMMENT 'Data e hora de término',
    
    -- Status do Agendamento
    status ENUM(
        'AGENDADO',
        'CONFIRMADO',
        'EM_ATENDIMENTO',
        'CONCLUIDO',
        'CANCELADO',
        'REMARCADO',
        'FALTOSO',
        'PACIENTE_NAO_COMPARECEU'
    ) NOT NULL DEFAULT 'AGENDADO',
    
    -- Local e Detalhes
    local VARCHAR(255) NULL COMMENT 'Sala, consultório, online, etc',
    modalidade ENUM('PRESENCIAL', 'ONLINE', 'DOMICILIAR') DEFAULT 'PRESENCIAL',
    
    -- Observações
    observacoes TEXT NULL COMMENT 'Observações gerais do agendamento',
    motivo_cancelamento TEXT NULL COMMENT 'Motivo se cancelado',
    observacoes_atendimento TEXT NULL COMMENT 'Observações após o atendimento',
    
    -- Histórico de Alterações
    agendamento_original_id BIGINT NULL COMMENT 'FK para agendamentos (se foi remarcado)',
    
    -- Lembretes e Notificações
    lembrete_enviado BOOLEAN DEFAULT FALSE,
    lembrete_enviado_em TIMESTAMP NULL,
    confirmacao_paciente BOOLEAN DEFAULT FALSE,
    confirmacao_paciente_em TIMESTAMP NULL,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL COMMENT 'FK para auth_usuarios que criou',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT NULL COMMENT 'FK para auth_usuarios que atualizou',
    canceled_at TIMESTAMP NULL,
    canceled_by BIGINT NULL COMMENT 'FK para auth_usuarios que cancelou',
    
    -- Índices para Performance
    INDEX idx_agendamentos_data_inicio (data_inicio),
    INDEX idx_agendamentos_data_fim (data_fim),
    INDEX idx_agendamentos_status (status),
    INDEX idx_agendamentos_paciente (paciente_id),
    INDEX idx_agendamentos_profissional (profissional_id),
    INDEX idx_agendamentos_tipo_servico (tipo_servico_id),
    INDEX idx_agendamentos_data_status (data_inicio, status),
    INDEX idx_agendamentos_profissional_data (profissional_id, data_inicio),
    
    -- Foreign Keys
    CONSTRAINT fk_agendamentos_tipo_servico FOREIGN KEY (tipo_servico_id) 
        REFERENCES tipos_servico(id) ON DELETE RESTRICT,
    CONSTRAINT fk_agendamentos_paciente FOREIGN KEY (paciente_id) 
        REFERENCES pacientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_agendamentos_profissional FOREIGN KEY (profissional_id) 
        REFERENCES profissionais(id) ON DELETE SET NULL,
    CONSTRAINT fk_agendamentos_acompanhante FOREIGN KEY (acompanhante_id) 
        REFERENCES acompanhantes(id) ON DELETE SET NULL,
    CONSTRAINT fk_agendamentos_original FOREIGN KEY (agendamento_original_id) 
        REFERENCES agendamentos(id) ON DELETE SET NULL,
    CONSTRAINT fk_agendamentos_created_by FOREIGN KEY (created_by) 
        REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_agendamentos_updated_by FOREIGN KEY (updated_by) 
        REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_agendamentos_canceled_by FOREIGN KEY (canceled_by) 
        REFERENCES auth_usuarios(id) ON DELETE SET NULL,
        
    -- Constraint para garantir que data_fim > data_inicio
    CONSTRAINT chk_agendamentos_datas CHECK (data_fim > data_inicio)
) ENGINE=InnoDB
COMMENT='Agendamentos de serviços e atendimentos individuais';
