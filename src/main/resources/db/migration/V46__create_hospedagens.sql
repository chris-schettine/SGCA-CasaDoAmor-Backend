-- Criação da tabela de hospedagens
-- Histórico de estadias dos pacientes na instituição

CREATE TABLE hospedagens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE COMMENT 'Identificador único UUID',
    
    -- Relacionamentos
    paciente_id CHAR(36) NOT NULL COMMENT 'Paciente hospedado',
    quarto_id BIGINT COMMENT 'Quarto onde está hospedado',
    
    -- Datas da Hospedagem
    data_entrada DATE NOT NULL COMMENT 'Data de entrada na instituição',
    hora_entrada TIME COMMENT 'Hora de entrada',
    data_saida_prevista DATE COMMENT 'Previsão de saída',
    data_saida DATE COMMENT 'Data efetiva de saída',
    hora_saida TIME COMMENT 'Hora de saída',
    
    -- Status
    status ENUM('ATIVA', 'ENCERRADA', 'TRANSFERENCIA') NOT NULL DEFAULT 'ATIVA' COMMENT 'Status da hospedagem',
    motivo_saida VARCHAR(255) COMMENT 'Motivo da saída (alta, transferência, óbito, etc.)',
    
    -- Observações
    observacoes_entrada TEXT COMMENT 'Observações no momento da entrada',
    observacoes_saida TEXT COMMENT 'Observações no momento da saída',
    observacoes_gerais TEXT COMMENT 'Observações gerais sobre a hospedagem',
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'Usuário que registrou a entrada',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT COMMENT 'Usuário que atualizou',
    deleted_at TIMESTAMP NULL COMMENT 'Soft delete',
    
    -- Foreign Keys
    CONSTRAINT fk_hospedagens_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_hospedagens_quarto FOREIGN KEY (quarto_id) REFERENCES quartos(id) ON DELETE SET NULL,
    CONSTRAINT fk_hospedagens_created_by FOREIGN KEY (created_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_hospedagens_updated_by FOREIGN KEY (updated_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    
    -- Validações
    CONSTRAINT chk_hospedagens_datas CHECK (
        data_saida IS NULL OR data_saida >= data_entrada
    ),
    CONSTRAINT chk_hospedagens_status_saida CHECK (
        (status = 'ATIVA' AND data_saida IS NULL) OR
        (status IN ('ENCERRADA', 'TRANSFERENCIA') AND data_saida IS NOT NULL)
    ),
    
    -- Índices
    INDEX idx_hospedagens_uuid (uuid),
    INDEX idx_hospedagens_paciente (paciente_id),
    INDEX idx_hospedagens_quarto (quarto_id),
    INDEX idx_hospedagens_status (status),
    INDEX idx_hospedagens_data_entrada (data_entrada),
    INDEX idx_hospedagens_data_saida (data_saida),
    INDEX idx_hospedagens_deleted_at (deleted_at),
    INDEX idx_hospedagens_ativa (status, data_saida) COMMENT 'Para buscar hospedagens ativas'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Histórico de hospedagens dos pacientes na instituição';
