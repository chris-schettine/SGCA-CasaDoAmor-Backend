-- Migration para criar a tabela unificada de profissionais (funcionários e voluntários)
-- UC3.1, UC3.2: Cadastro de Funcionário e Voluntário

CREATE TABLE profissionais (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid CHAR(36) UNIQUE NOT NULL,
    
    -- Dados Pessoais
    nome VARCHAR(255) NOT NULL,
    cpf_criptografado VARBINARY(512) NULL COMMENT 'CPF criptografado com AES-256',
    telefone VARCHAR(20) NULL,
    email VARCHAR(255) NULL,
    
    -- Categoria e Área de Atuação
    categoria ENUM('FUNCIONARIO', 'VOLUNTARIO') NOT NULL COMMENT 'Tipo de profissional',
    area_atuacao VARCHAR(100) NULL COMMENT 'SAUDE, SUPORTE, ADMINISTRATIVO, MOTORISTA, etc',
    especialidade VARCHAR(255) NULL COMMENT 'Médico, Enfermeiro, Nutricionista, Fisioterapeuta, Dentista, etc',
    numero_registro VARCHAR(50) NULL COMMENT 'CRM, CRO, COREN, CREFITO, CRN, etc',
    uf_registro CHAR(2) NULL COMMENT 'UF do registro profissional',
    
    -- Dados Trabalhistas (apenas para FUNCIONARIO)
    data_admissao DATE NULL COMMENT 'Data de admissão do funcionário',
    data_desligamento DATE NULL COMMENT 'Data de desligamento',
    tipo_contrato VARCHAR(50) NULL COMMENT 'CLT, PJ, TEMPORARIO, etc',
    carga_horaria INT NULL COMMENT 'Horas semanais',
    cargo VARCHAR(100) NULL COMMENT 'Cargo/função',
    departamento VARCHAR(100) NULL,
    dados_bancarios_criptografados VARBINARY(1024) NULL COMMENT 'Informações bancárias criptografadas (JSON)',
    
    -- Disponibilidade (principalmente para VOLUNTARIO)
    disponibilidade JSON NULL COMMENT 'Horários e dias disponíveis: {"segunda": ["08:00-12:00"], "terca": ["14:00-18:00"]}',
    
    -- Dados de Endereço (referência à tabela existente)
    endereco_id CHAR(36) NULL COMMENT 'FK para enderecos',
    
    -- Status e Controle
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    observacoes TEXT NULL,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL COMMENT 'FK para auth_usuarios',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT NULL COMMENT 'FK para auth_usuarios',
    deleted_at TIMESTAMP NULL COMMENT 'Soft delete',
    
    -- Índices
    INDEX idx_profissionais_categoria (categoria),
    INDEX idx_profissionais_area_atuacao (area_atuacao),
    INDEX idx_profissionais_especialidade (especialidade),
    INDEX idx_profissionais_ativo (ativo),
    INDEX idx_profissionais_nome (nome),
    
    -- Foreign Keys
    CONSTRAINT fk_profissionais_endereco FOREIGN KEY (endereco_id) REFERENCES enderecos(id) ON DELETE SET NULL,
    CONSTRAINT fk_profissionais_created_by FOREIGN KEY (created_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_profissionais_updated_by FOREIGN KEY (updated_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabela unificada de funcionários e voluntários';
