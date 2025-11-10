-- Migration para criar tabela de atividades em grupo
-- UC3.8, UC5.2: Criar Atividade em Grupo (oficinas, palestras, eventos)

CREATE TABLE atividades_grupo (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid CHAR(36) UNIQUE NOT NULL,
    
    -- Informações da Atividade
    titulo VARCHAR(255) NOT NULL COMMENT 'Título da atividade',
    descricao TEXT NULL COMMENT 'Descrição detalhada da atividade',
    tipo VARCHAR(100) NULL COMMENT 'Tipo: OFICINA, PALESTRA, GRUPO_APOIO, RECREACAO, EDUCATIVA, etc',
    
    -- Data e Hora
    data_inicio DATETIME NOT NULL COMMENT 'Data e hora de início',
    data_fim DATETIME NOT NULL COMMENT 'Data e hora de término',
    
    -- Local
    local VARCHAR(255) NULL COMMENT 'Local onde será realizada',
    modalidade ENUM('PRESENCIAL', 'ONLINE', 'HIBRIDA') DEFAULT 'PRESENCIAL',
    
    -- Capacidade e Público-Alvo
    capacidade INT NULL COMMENT 'Número máximo de participantes (null = ilimitado)',
    publico_alvo VARCHAR(255) NULL COMMENT 'Ex: crianças 6-10 anos, adolescentes, mães, etc',
    idade_minima INT NULL,
    idade_maxima INT NULL,
    
    -- Responsáveis
    responsavel_principal_id BIGINT NULL COMMENT 'FK para profissionais',
    responsavel_apoio_id BIGINT NULL COMMENT 'FK para profissionais (apoio)',
    
    -- Materiais e Recursos
    materiais JSON NULL COMMENT 'Lista de materiais necessários: ["papel", "canetinhas", "datashow"]',
    
    -- Status
    status ENUM(
        'PLANEJADA',
        'CONFIRMADA',
        'EM_ANDAMENTO',
        'CONCLUIDA',
        'CANCELADA',
        'ADIADA'
    ) NOT NULL DEFAULT 'PLANEJADA',
    
    -- Observações e Resultados
    observacoes TEXT NULL COMMENT 'Observações gerais',
    observacoes_pos_atividade TEXT NULL COMMENT 'Relatório/feedback após a atividade',
    motivo_cancelamento TEXT NULL,
    
    -- Inscrições
    requer_inscricao BOOLEAN DEFAULT FALSE COMMENT 'Se requer inscrição prévia',
    inscricoes_abertas BOOLEAN DEFAULT TRUE,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL COMMENT 'FK para auth_usuarios',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT NULL COMMENT 'FK para auth_usuarios',
    canceled_at TIMESTAMP NULL,
    canceled_by BIGINT NULL,
    
    -- Índices
    INDEX idx_atividades_data_inicio (data_inicio),
    INDEX idx_atividades_data_fim (data_fim),
    INDEX idx_atividades_status (status),
    INDEX idx_atividades_tipo (tipo),
    INDEX idx_atividades_responsavel (responsavel_principal_id),
    INDEX idx_atividades_data_status (data_inicio, status),
    
    -- Foreign Keys
    CONSTRAINT fk_atividades_responsavel_principal FOREIGN KEY (responsavel_principal_id) 
        REFERENCES profissionais(id) ON DELETE SET NULL,
    CONSTRAINT fk_atividades_responsavel_apoio FOREIGN KEY (responsavel_apoio_id) 
        REFERENCES profissionais(id) ON DELETE SET NULL,
    CONSTRAINT fk_atividades_created_by FOREIGN KEY (created_by) 
        REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_atividades_updated_by FOREIGN KEY (updated_by) 
        REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_atividades_canceled_by FOREIGN KEY (canceled_by) 
        REFERENCES auth_usuarios(id) ON DELETE SET NULL,
        
    -- Constraint para garantir que data_fim > data_inicio
    CONSTRAINT chk_atividades_datas CHECK (data_fim > data_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Atividades em grupo: oficinas, palestras, eventos coletivos';
