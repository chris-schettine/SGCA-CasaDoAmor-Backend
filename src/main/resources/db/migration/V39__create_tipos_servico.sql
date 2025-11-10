-- Migration para criar catálogo de tipos de serviços
-- UC5.5, UC5.6: Cadastro de tipos de serviço (medicina, odontologia, enfermagem, etc.)

CREATE TABLE tipos_servico (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Identificação
    codigo VARCHAR(50) UNIQUE NOT NULL COMMENT 'Código único do serviço (ex: MED_CONSULTA_GERAL)',
    nome VARCHAR(255) NOT NULL COMMENT 'Nome do serviço',
    descricao TEXT NULL COMMENT 'Descrição detalhada do serviço',
    
    -- Categoria do Serviço
    categoria ENUM(
        'MEDICO',
        'ODONTOLOGICO', 
        'ENFERMAGEM',
        'NUTRICAO',
        'FISIOTERAPIA',
        'PSICOLOGIA',
        'ASSISTENCIA_SOCIAL',
        'PEDAGOGIA',
        'OUTROS'
    ) NOT NULL COMMENT 'Categoria principal do serviço',
    
    -- Duração e Configurações
    duracao_minutos INT NOT NULL DEFAULT 30 COMMENT 'Duração estimada em minutos',
    requer_profissional BOOLEAN DEFAULT TRUE COMMENT 'Se requer profissional específico',
    permite_acompanhante BOOLEAN DEFAULT TRUE COMMENT 'Se permite acompanhante no atendimento',
    
    -- Status
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Observações e Requisitos
    observacoes TEXT NULL COMMENT 'Requisitos, preparações necessárias, etc',
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL COMMENT 'FK para auth_usuarios',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT NULL COMMENT 'FK para auth_usuarios',
    
    -- Índices
    INDEX idx_tipos_servico_categoria (categoria),
    INDEX idx_tipos_servico_ativo (ativo),
    INDEX idx_tipos_servico_codigo (codigo),
    
    -- Foreign Keys
    CONSTRAINT fk_tipos_servico_created_by FOREIGN KEY (created_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_tipos_servico_updated_by FOREIGN KEY (updated_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Catálogo de tipos de serviços oferecidos';

-- Inserir alguns tipos de serviço padrão
INSERT INTO tipos_servico (codigo, nome, descricao, categoria, duracao_minutos, created_at) VALUES
-- Médicos
('MED_CONSULTA_GERAL', 'Consulta Médica Geral', 'Consulta médica de rotina', 'MEDICO', 30, NOW()),
('MED_CONSULTA_ESPECIALISTA', 'Consulta com Especialista', 'Consulta com médico especialista', 'MEDICO', 45, NOW()),
('MED_RETORNO', 'Retorno Médico', 'Consulta de retorno', 'MEDICO', 20, NOW()),

-- Odontológicos
('ODONTO_CONSULTA', 'Consulta Odontológica', 'Avaliação odontológica inicial', 'ODONTOLOGICO', 30, NOW()),
('ODONTO_PROFILAXIA', 'Profilaxia Dentária', 'Limpeza e profilaxia', 'ODONTOLOGICO', 40, NOW()),
('ODONTO_RESTAURACAO', 'Restauração Dentária', 'Procedimento de restauração', 'ODONTOLOGICO', 60, NOW()),

-- Enfermagem
('ENF_CURATIVO', 'Curativo', 'Realização de curativo', 'ENFERMAGEM', 20, NOW()),
('ENF_AFERIR_PA', 'Aferição de Pressão Arterial', 'Verificação de pressão arterial', 'ENFERMAGEM', 10, NOW()),
('ENF_MEDICACAO', 'Administração de Medicação', 'Administração de medicamentos prescritos', 'ENFERMAGEM', 15, NOW()),
('ENF_COLETA', 'Coleta de Exames', 'Coleta de materiais para exames', 'ENFERMAGEM', 15, NOW()),

-- Nutrição
('NUT_CONSULTA', 'Consulta Nutricional', 'Avaliação e orientação nutricional', 'NUTRICAO', 40, NOW()),
('NUT_RETORNO', 'Retorno Nutricional', 'Acompanhamento nutricional', 'NUTRICAO', 30, NOW()),

-- Fisioterapia
('FISIO_SESSAO', 'Sessão de Fisioterapia', 'Sessão de fisioterapia', 'FISIOTERAPIA', 45, NOW()),
('FISIO_AVALIACAO', 'Avaliação Fisioterapêutica', 'Avaliação inicial fisioterapêutica', 'FISIOTERAPIA', 50, NOW()),

-- Psicologia
('PSI_ATENDIMENTO', 'Atendimento Psicológico', 'Sessão de atendimento psicológico', 'PSICOLOGIA', 50, NOW()),
('PSI_AVALIACAO', 'Avaliação Psicológica', 'Avaliação psicológica inicial', 'PSICOLOGIA', 60, NOW()),

-- Assistência Social
('AS_ATENDIMENTO', 'Atendimento Social', 'Atendimento assistência social', 'ASSISTENCIA_SOCIAL', 40, NOW()),

-- Pedagogia
('PED_ATENDIMENTO', 'Atendimento Pedagógico', 'Acompanhamento pedagógico', 'PEDAGOGIA', 45, NOW());
