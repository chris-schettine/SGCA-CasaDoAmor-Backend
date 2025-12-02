-- Migration para reestruturar sistema de agendamentos
-- Separando agendamentos de pacientes e acompanhantes
-- Vinculando profissionais de saúde aos usuários do sistema

-- =====================================================
-- TABELA: agendamentos_pacientes
-- Agendamentos de serviços para pacientes
-- =====================================================
CREATE TABLE agendamentos_pacientes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid CHAR(36) UNIQUE NOT NULL COMMENT 'Identificador único',
    
    -- Relacionamentos principais
    paciente_id CHAR(36) NOT NULL COMMENT 'FK para pacientes',
    tipo_servico_id BIGINT NOT NULL COMMENT 'FK para tipos_servico',
    profissional_usuario_id BIGINT NULL COMMENT 'FK para auth_usuarios (profissional de saúde)',
    hospedagem_id BIGINT NULL COMMENT 'FK para hospedagens (contexto da hospedagem)',
    
    -- Data e Hora
    data_hora_inicio DATETIME NOT NULL COMMENT 'Início do atendimento',
    data_hora_fim DATETIME NOT NULL COMMENT 'Fim previsto do atendimento',
    duracao_minutos INT NOT NULL COMMENT 'Duração em minutos',
    
    -- Status do Agendamento
    status ENUM(
        'AGENDADO',           -- Criado, aguardando confirmação
        'CONFIRMADO',         -- Paciente/profissional confirmou
        'EM_ATENDIMENTO',     -- Atendimento em andamento
        'CONCLUIDO',          -- Atendimento finalizado
        'CANCELADO',          -- Cancelado por algum motivo
        'REMARCADO',          -- Foi remarcado (aponta para novo agendamento)
        'NAO_COMPARECEU'      -- Paciente faltou
    ) NOT NULL DEFAULT 'AGENDADO',
    
    -- Tipo de Agendamento
    tipo_atendimento ENUM('PRIMEIRA_VEZ', 'RETORNO', 'EMERGENCIAL', 'ROTINA', 'TRIAGEM') 
        NOT NULL DEFAULT 'ROTINA',
    
    -- Modalidade
    modalidade ENUM('PRESENCIAL', 'ONLINE', 'DOMICILIAR') DEFAULT 'PRESENCIAL',
    
    -- Local
    local_atendimento VARCHAR(255) NULL COMMENT 'Sala, consultório, enfermaria, etc',
    
    -- Prioridade
    prioridade ENUM('BAIXA', 'NORMAL', 'ALTA', 'URGENTE') DEFAULT 'NORMAL',
    
    -- Automatização (para triagens automáticas)
    gerado_automaticamente BOOLEAN DEFAULT FALSE COMMENT 'Se foi criado automaticamente',
    motivo_geracao_automatica VARCHAR(255) NULL COMMENT 'Ex: Entrada em hospedagem',
    
    -- Observações e Detalhes
    observacoes TEXT NULL COMMENT 'Observações gerais',
    observacoes_profissional TEXT NULL COMMENT 'Observações do profissional após atendimento',
    motivo_cancelamento TEXT NULL COMMENT 'Motivo do cancelamento',
    
    -- Remarcação
    agendamento_original_id BIGINT NULL COMMENT 'FK para agendamentos_pacientes (se remarcado)',
    novo_agendamento_id BIGINT NULL COMMENT 'FK para agendamentos_pacientes (novo agendamento)',
    
    -- Confirmações e Lembretes
    confirmado_paciente BOOLEAN DEFAULT FALSE,
    confirmado_paciente_em DATETIME NULL,
    confirmado_profissional BOOLEAN DEFAULT FALSE,
    confirmado_profissional_em DATETIME NULL,
    lembrete_enviado BOOLEAN DEFAULT FALSE,
    lembrete_enviado_em DATETIME NULL,
    
    -- Resultado do Atendimento
    compareceu BOOLEAN NULL COMMENT 'NULL=não atendido ainda, TRUE=compareceu, FALSE=faltou',
    hora_chegada DATETIME NULL COMMENT 'Hora real que o paciente chegou',
    hora_inicio_atendimento DATETIME NULL COMMENT 'Hora real que iniciou o atendimento',
    hora_fim_atendimento DATETIME NULL COMMENT 'Hora real que finalizou',
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL COMMENT 'Usuário que criou',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT NULL COMMENT 'Usuário que atualizou',
    canceled_at TIMESTAMP NULL,
    canceled_by BIGINT NULL COMMENT 'Usuário que cancelou',
    deleted_at TIMESTAMP NULL COMMENT 'Soft delete',
    
    -- Constraints
    CONSTRAINT chk_agendamentos_pacientes_datas 
        CHECK (data_hora_fim > data_hora_inicio),
    CONSTRAINT chk_agendamentos_pacientes_duracao 
        CHECK (duracao_minutos > 0),
    
    -- Índices para performance e validação
    INDEX idx_agend_pac_paciente (paciente_id),
    INDEX idx_agend_pac_profissional (profissional_usuario_id),
    INDEX idx_agend_pac_tipo_servico (tipo_servico_id),
    INDEX idx_agend_pac_data_inicio (data_hora_inicio),
    INDEX idx_agend_pac_status (status),
    INDEX idx_agend_pac_hospedagem (hospedagem_id),
    INDEX idx_agend_pac_profissional_data (profissional_usuario_id, data_hora_inicio),
    INDEX idx_agend_pac_status_data (status, data_hora_inicio),
    INDEX idx_agend_pac_deleted (deleted_at),
    
    -- Índice composto para detectar conflitos de horário do profissional
    INDEX idx_agend_pac_conflito (profissional_usuario_id, data_hora_inicio, data_hora_fim, status),
    
    -- Foreign Keys
    CONSTRAINT fk_agend_pac_paciente 
        FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_agend_pac_tipo_servico 
        FOREIGN KEY (tipo_servico_id) REFERENCES tipos_servico(id) ON DELETE RESTRICT,
    CONSTRAINT fk_agend_pac_profissional 
        FOREIGN KEY (profissional_usuario_id) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_agend_pac_hospedagem 
        FOREIGN KEY (hospedagem_id) REFERENCES hospedagens(id) ON DELETE SET NULL,
    CONSTRAINT fk_agend_pac_original 
        FOREIGN KEY (agendamento_original_id) REFERENCES agendamentos_pacientes(id) ON DELETE SET NULL,
    CONSTRAINT fk_agend_pac_novo 
        FOREIGN KEY (novo_agendamento_id) REFERENCES agendamentos_pacientes(id) ON DELETE SET NULL,
    CONSTRAINT fk_agend_pac_created_by 
        FOREIGN KEY (created_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_agend_pac_updated_by 
        FOREIGN KEY (updated_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_agend_pac_canceled_by 
        FOREIGN KEY (canceled_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL
        
) ENGINE=InnoDB
COMMENT='Agendamentos de serviços para pacientes';

-- =====================================================
-- TABELA: agendamentos_acompanhantes
-- Agendamentos de serviços para acompanhantes
-- =====================================================
CREATE TABLE agendamentos_acompanhantes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid CHAR(36) UNIQUE NOT NULL COMMENT 'Identificador único',
    
    -- Relacionamentos principais
    acompanhante_id CHAR(36) NOT NULL COMMENT 'FK para acompanhantes',
    tipo_servico_id BIGINT NOT NULL COMMENT 'FK para tipos_servico',
    profissional_usuario_id BIGINT NULL COMMENT 'FK para auth_usuarios (profissional de saúde)',
    paciente_vinculado_id CHAR(36) NULL COMMENT 'FK para pacientes (paciente que está acompanhando)',
    
    -- Data e Hora
    data_hora_inicio DATETIME NOT NULL COMMENT 'Início do atendimento',
    data_hora_fim DATETIME NOT NULL COMMENT 'Fim previsto do atendimento',
    duracao_minutos INT NOT NULL COMMENT 'Duração em minutos',
    
    -- Status do Agendamento
    status ENUM(
        'AGENDADO',
        'CONFIRMADO',
        'EM_ATENDIMENTO',
        'CONCLUIDO',
        'CANCELADO',
        'REMARCADO',
        'NAO_COMPARECEU'
    ) NOT NULL DEFAULT 'AGENDADO',
    
    -- Tipo de Atendimento
    tipo_atendimento ENUM('PRIMEIRA_VEZ', 'RETORNO', 'EMERGENCIAL', 'ROTINA', 'TRIAGEM') 
        NOT NULL DEFAULT 'ROTINA',
    
    -- Modalidade
    modalidade ENUM('PRESENCIAL', 'ONLINE', 'DOMICILIAR') DEFAULT 'PRESENCIAL',
    
    -- Local
    local_atendimento VARCHAR(255) NULL COMMENT 'Sala, consultório, etc',
    
    -- Prioridade
    prioridade ENUM('BAIXA', 'NORMAL', 'ALTA', 'URGENTE') DEFAULT 'NORMAL',
    
    -- Observações
    observacoes TEXT NULL COMMENT 'Observações gerais',
    observacoes_profissional TEXT NULL COMMENT 'Observações do profissional',
    motivo_cancelamento TEXT NULL COMMENT 'Motivo do cancelamento',
    
    -- Remarcação
    agendamento_original_id BIGINT NULL COMMENT 'FK para agendamentos_acompanhantes',
    novo_agendamento_id BIGINT NULL COMMENT 'FK para agendamentos_acompanhantes',
    
    -- Confirmações e Lembretes
    confirmado_acompanhante BOOLEAN DEFAULT FALSE,
    confirmado_acompanhante_em DATETIME NULL,
    confirmado_profissional BOOLEAN DEFAULT FALSE,
    confirmado_profissional_em DATETIME NULL,
    lembrete_enviado BOOLEAN DEFAULT FALSE,
    lembrete_enviado_em DATETIME NULL,
    
    -- Resultado do Atendimento
    compareceu BOOLEAN NULL,
    hora_chegada DATETIME NULL,
    hora_inicio_atendimento DATETIME NULL,
    hora_fim_atendimento DATETIME NULL,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    canceled_at TIMESTAMP NULL,
    canceled_by BIGINT NULL,
    deleted_at TIMESTAMP NULL COMMENT 'Soft delete',
    
    -- Constraints
    CONSTRAINT chk_agendamentos_acomp_datas 
        CHECK (data_hora_fim > data_hora_inicio),
    CONSTRAINT chk_agendamentos_acomp_duracao 
        CHECK (duracao_minutos > 0),
    
    -- Índices
    INDEX idx_agend_acomp_acompanhante (acompanhante_id),
    INDEX idx_agend_acomp_profissional (profissional_usuario_id),
    INDEX idx_agend_acomp_tipo_servico (tipo_servico_id),
    INDEX idx_agend_acomp_data_inicio (data_hora_inicio),
    INDEX idx_agend_acomp_status (status),
    INDEX idx_agend_acomp_paciente (paciente_vinculado_id),
    INDEX idx_agend_acomp_profissional_data (profissional_usuario_id, data_hora_inicio),
    INDEX idx_agend_acomp_status_data (status, data_hora_inicio),
    INDEX idx_agend_acomp_deleted (deleted_at),
    INDEX idx_agend_acomp_conflito (profissional_usuario_id, data_hora_inicio, data_hora_fim, status),
    
    -- Foreign Keys
    CONSTRAINT fk_agend_acomp_acompanhante 
        FOREIGN KEY (acompanhante_id) REFERENCES acompanhantes(id) ON DELETE CASCADE,
    CONSTRAINT fk_agend_acomp_tipo_servico 
        FOREIGN KEY (tipo_servico_id) REFERENCES tipos_servico(id) ON DELETE RESTRICT,
    CONSTRAINT fk_agend_acomp_profissional 
        FOREIGN KEY (profissional_usuario_id) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_agend_acomp_paciente 
        FOREIGN KEY (paciente_vinculado_id) REFERENCES pacientes(id) ON DELETE SET NULL,
    CONSTRAINT fk_agend_acomp_original 
        FOREIGN KEY (agendamento_original_id) REFERENCES agendamentos_acompanhantes(id) ON DELETE SET NULL,
    CONSTRAINT fk_agend_acomp_novo 
        FOREIGN KEY (novo_agendamento_id) REFERENCES agendamentos_acompanhantes(id) ON DELETE SET NULL,
    CONSTRAINT fk_agend_acomp_created_by 
        FOREIGN KEY (created_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_agend_acomp_updated_by 
        FOREIGN KEY (updated_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_agend_acomp_canceled_by 
        FOREIGN KEY (canceled_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL
        
) ENGINE=InnoDB
COMMENT='Agendamentos de serviços para acompanhantes';

-- =====================================================
-- TABELA: profissionais_especialidades
-- Vincula profissionais (auth_usuarios) com suas especialidades
-- =====================================================
CREATE TABLE profissionais_especialidades (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    profissional_usuario_id BIGINT NOT NULL COMMENT 'FK para auth_usuarios',
    categoria_servico ENUM(
        'MEDICO',
        'ODONTOLOGICO',
        'ENFERMAGEM',
        'NUTRICAO',
        'FISIOTERAPIA',
        'PSICOLOGIA',
        'ASSISTENCIA_SOCIAL',
        'PEDAGOGIA',
        'OUTROS'
    ) NOT NULL COMMENT 'Categoria/especialidade do profissional',
    
    -- Dados Profissionais
    registro_profissional VARCHAR(50) NULL COMMENT 'CRM, CRO, COREN, CRN, etc',
    registro_uf CHAR(2) NULL COMMENT 'UF do registro profissional',
    especialidade VARCHAR(255) NULL COMMENT 'Especialidade dentro da categoria',
    
    -- Status
    ativo BOOLEAN DEFAULT TRUE,
    disponivel_agendamento BOOLEAN DEFAULT TRUE COMMENT 'Se aceita novos agendamentos',
    
    -- Configurações de Agenda
    duracao_padrao_consulta INT DEFAULT 30 COMMENT 'Duração padrão em minutos',
    intervalo_entre_consultas INT DEFAULT 0 COMMENT 'Tempo de intervalo entre consultas',
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    
    -- Índices
    INDEX idx_prof_esp_usuario (profissional_usuario_id),
    INDEX idx_prof_esp_categoria (categoria_servico),
    INDEX idx_prof_esp_ativo (ativo),
    INDEX idx_prof_esp_disponivel (disponivel_agendamento),
    
    -- Constraint única: um profissional pode ter várias especialidades, mas não pode repetir
    UNIQUE KEY uk_prof_esp (profissional_usuario_id, categoria_servico),
    
    -- Foreign Key
    CONSTRAINT fk_prof_esp_usuario 
        FOREIGN KEY (profissional_usuario_id) REFERENCES auth_usuarios(id) ON DELETE CASCADE
        
) ENGINE=InnoDB
COMMENT='Especialidades dos profissionais de saúde';

-- =====================================================
-- TABELA: horarios_profissionais
-- Define os horários de trabalho dos profissionais
-- =====================================================
CREATE TABLE horarios_profissionais (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    profissional_usuario_id BIGINT NOT NULL COMMENT 'FK para auth_usuarios',
    
    -- Dia da semana (1=Segunda, 7=Domingo)
    dia_semana TINYINT NOT NULL COMMENT '1=Segunda, 2=Terça, ..., 7=Domingo',
    
    -- Horários
    hora_inicio TIME NOT NULL COMMENT 'Hora de início do expediente',
    hora_fim TIME NOT NULL COMMENT 'Hora de fim do expediente',
    
    -- Status
    ativo BOOLEAN DEFAULT TRUE,
    
    -- Observações
    observacoes VARCHAR(255) NULL COMMENT 'Ex: Apenas emergências, etc',
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_horarios_prof_dia 
        CHECK (dia_semana BETWEEN 1 AND 7),
    CONSTRAINT chk_horarios_prof_horas 
        CHECK (hora_fim > hora_inicio),
    
    -- Índices
    INDEX idx_horarios_prof_usuario (profissional_usuario_id),
    INDEX idx_horarios_prof_dia (dia_semana),
    INDEX idx_horarios_prof_ativo (ativo),
    
    -- Foreign Key
    CONSTRAINT fk_horarios_prof_usuario 
        FOREIGN KEY (profissional_usuario_id) REFERENCES auth_usuarios(id) ON DELETE CASCADE
        
) ENGINE=InnoDB
COMMENT='Horários de trabalho dos profissionais';

-- =====================================================
-- TABELA: bloqueios_agenda
-- Bloqueios de agenda (férias, reuniões, etc)
-- =====================================================
CREATE TABLE bloqueios_agenda (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    profissional_usuario_id BIGINT NOT NULL COMMENT 'FK para auth_usuarios',
    
    -- Período do bloqueio
    data_hora_inicio DATETIME NOT NULL,
    data_hora_fim DATETIME NOT NULL,
    
    -- Tipo de bloqueio
    tipo ENUM('FERIAS', 'REUNIAO', 'TREINAMENTO', 'LICENCA', 'OUTROS') NOT NULL,
    
    -- Detalhes
    motivo VARCHAR(500) NULL,
    
    -- Recorrência (futuro)
    recorrente BOOLEAN DEFAULT FALSE,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_bloqueios_datas 
        CHECK (data_hora_fim > data_hora_inicio),
    
    -- Índices
    INDEX idx_bloqueios_profissional (profissional_usuario_id),
    INDEX idx_bloqueios_data_inicio (data_hora_inicio),
    INDEX idx_bloqueios_periodo (profissional_usuario_id, data_hora_inicio, data_hora_fim),
    
    -- Foreign Keys
    CONSTRAINT fk_bloqueios_usuario 
        FOREIGN KEY (profissional_usuario_id) REFERENCES auth_usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_bloqueios_created_by 
        FOREIGN KEY (created_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL
        
) ENGINE=InnoDB
COMMENT='Bloqueios de agenda dos profissionais';
