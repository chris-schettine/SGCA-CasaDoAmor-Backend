-- Migration para criar tabela de presenças em atividades
-- UC3.10, UC5.4: Registrar Participação de Assistidos em Atividades

CREATE TABLE presencas_atividade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Relacionamentos
    atividade_id BIGINT NOT NULL COMMENT 'FK para atividades_grupo',
    paciente_id CHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'FK para pacientes',
    
    -- Presença e Participação
    presente BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Se compareceu',
    horario_chegada TIME NULL COMMENT 'Hora que chegou',
    horario_saida TIME NULL COMMENT 'Hora que saiu',
    
    -- Avaliação da Participação
    nivel_participacao ENUM(
        'NAO_PARTICIPOU',
        'BAIXA',
        'MEDIA',
        'ALTA',
        'EXCELENTE'
    ) NULL COMMENT 'Nível de engajamento',
    
    -- Observações
    observacoes TEXT NULL COMMENT 'Observações sobre a participação',
    
    -- Registro
    registrado_por BIGINT NULL COMMENT 'FK para auth_usuarios que registrou',
    registrado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    
    -- Índices
    INDEX idx_presencas_atividade (atividade_id),
    INDEX idx_presencas_paciente (paciente_id),
    INDEX idx_presencas_presente (presente),
    INDEX idx_presencas_atividade_presente (atividade_id, presente),
    
    -- Foreign Keys
    CONSTRAINT fk_presencas_atividade FOREIGN KEY (atividade_id) 
        REFERENCES atividades_grupo(id) ON DELETE CASCADE,
    CONSTRAINT fk_presencas_paciente FOREIGN KEY (paciente_id) 
        REFERENCES pacientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_presencas_registrado_por FOREIGN KEY (registrado_por) 
        REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    
    -- Unicidade: um paciente não pode ter dois registros de presença na mesma atividade
    UNIQUE KEY uk_presencas_atividade_paciente (atividade_id, paciente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Registro de presenças em atividades em grupo';

-- Tabela auxiliar para inscrições em atividades (quando requer_inscricao = true)
CREATE TABLE inscricoes_atividade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Relacionamentos
    atividade_id BIGINT NOT NULL COMMENT 'FK para atividades_grupo',
    paciente_id CHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'FK para pacientes',
    
    -- Status da Inscrição
    status ENUM(
        'PENDENTE',
        'CONFIRMADA',
        'LISTA_ESPERA',
        'CANCELADA'
    ) NOT NULL DEFAULT 'PENDENTE',
    
    -- Datas
    inscrito_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmado_em TIMESTAMP NULL,
    cancelado_em TIMESTAMP NULL,
    
    -- Quem inscreveu
    inscrito_por BIGINT NULL COMMENT 'FK para auth_usuarios',
    
    -- Observações
    observacoes TEXT NULL,
    
    -- Índices
    INDEX idx_inscricoes_atividade (atividade_id),
    INDEX idx_inscricoes_paciente (paciente_id),
    INDEX idx_inscricoes_status (status),
    
    -- Foreign Keys
    CONSTRAINT fk_inscricoes_atividade FOREIGN KEY (atividade_id) 
        REFERENCES atividades_grupo(id) ON DELETE CASCADE,
    CONSTRAINT fk_inscricoes_paciente FOREIGN KEY (paciente_id) 
        REFERENCES pacientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_inscricoes_inscrito_por FOREIGN KEY (inscrito_por) 
        REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    
    -- Unicidade: um paciente não pode se inscrever duas vezes na mesma atividade
    UNIQUE KEY uk_inscricoes_atividade_paciente (atividade_id, paciente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Inscrições em atividades que requerem inscrição prévia';
