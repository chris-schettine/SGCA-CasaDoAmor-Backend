-- ===========================================
--  MÓDULO DE AUTENTICAÇÃO - SISTEMA CASA DO AMOR
--  Autor: João Henrique Silva Pinto
--  Versão: 1.0
-- ===========================================

-- ===========================================
-- 1. TABELA: auth_usuarios
-- ===========================================
CREATE TABLE auth_usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid CHAR (36) UNIQUE NOT NULL,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    cpf VARCHAR(12) NOT NULL,
    ultimo_login_em TIMESTAMP NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    tipo ENUM ('ADMINISTRADOR', 'DENTISTA', 'ENFERMEIRO', 'FISIOTERAPEUTA', 'MEDICO', 'NUTRICIONISTA', 'RECEPCIONISTA', 'AUDITOR') DEFAULT 'RECEPCIONISTA',
    tentativas_falhas_de_login INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    email_verificado BOOLEAN DEFAULT FALSE,
    ultima_alteracao_senha_em TIMESTAMP NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por BIGINT NULL,
    atualizado_em TIMESTAMP NULL,
    atualizado_por BIGINT NULL,
    deletado_em TIMESTAMP NULL,
    metadados JSON NULL,
    
    FOREIGN KEY (criado_por) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (atualizado_por) REFERENCES auth_usuarios(id) ON DELETE SET NULL
);

CREATE INDEX idx_auth_usuarios_email ON auth_usuarios(email);
CREATE INDEX idx_auth_usuarios_uuid ON auth_usuarios(uuid);
CREATE INDEX idx_auth_usuarios_cpf ON auth_usuarios(cpf);

-- ===========================================
-- 2. TABELA: perfis (roles)
-- ===========================================
CREATE TABLE perfis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(50) UNIQUE NOT NULL,
    descricao TEXT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por BIGINT NULL,
    atualizado_em TIMESTAMP NULL,
    atualizado_por BIGINT NULL,
    deletado_em TIMESTAMP NULL
);

-- ===========================================
-- 3. TABELA: permissoes
-- ===========================================
CREATE TABLE permissoes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) UNIQUE NOT NULL,
    descricao TEXT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por BIGINT NULL,
    atualizado_em TIMESTAMP NULL,
    atualizado_por BIGINT NULL,
    deletado_em TIMESTAMP NULL
);

-- ===========================================
-- 4. TABELA: perfis_permissoes (N:N)
-- ===========================================
CREATE TABLE perfis_permissoes (
    perfil_id BIGINT NOT NULL,
    permissao_id BIGINT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (perfil_id, permissao_id),
    FOREIGN KEY (perfil_id) REFERENCES perfis(id) ON DELETE CASCADE,
    FOREIGN KEY (permissao_id) REFERENCES permissoes(id) ON DELETE CASCADE
);

-- ===========================================
-- 5. TABELA: auth_usuarios_perfis (N:N)
-- ===========================================
CREATE TABLE auth_usuarios_perfis (
    usuario_id BIGINT NOT NULL,
    perfil_id BIGINT NOT NULL,
    atribuido_por BIGINT NULL,
    atribuido_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, perfil_id),
    FOREIGN KEY (usuario_id) REFERENCES auth_usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (perfil_id) REFERENCES perfis(id) ON DELETE CASCADE,
    FOREIGN KEY (atribuido_por) REFERENCES auth_usuarios(id) ON DELETE SET NULL
);

-- ===========================================
-- 6. TABELA: areas_sistema
-- ===========================================
CREATE TABLE areas_sistema (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    descricao TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================
-- 7. TABELA: perfis_areas (N:N)
-- ===========================================
CREATE TABLE perfis_areas (
    perfil_id BIGINT NOT NULL,
    area_id BIGINT NOT NULL,
    nivel_acesso ENUM ('LEITURA', 'ESCRITA', 'ADMIN') NOT NULL DEFAULT 'LEITURA',
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (perfil_id, area_id),
    FOREIGN KEY (perfil_id) REFERENCES perfis(id) ON DELETE CASCADE,
    FOREIGN KEY (area_id) REFERENCES areas_sistema(id) ON DELETE CASCADE
);

-- ===========================================
-- 8. TABELA: tentativas_login
-- ===========================================
CREATE TABLE tentativas_login (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NULL,
    cpf VARCHAR(12) NOT NULL,
    ip_origem VARCHAR(45) NOT NULL,
    user_agent TEXT NULL,
    data_tentativa TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sucesso BOOLEAN NOT NULL,
    motivo_falha VARCHAR(255) NULL,
    bloqueado BOOLEAN DEFAULT FALSE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES auth_usuarios(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX idx_tentativas_login_cpf_data ON tentativas_login(cpf, data_tentativa);

-- ===========================================
-- 9. TABELA: tokens_recuperacao
-- ===========================================
CREATE TABLE tokens_recuperacao (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    tipo ENUM ('VERIFICACAO_EMAIL', 'RECUPERACAO_SENHA', 'AUTENTICACAO_2FA') NOT NULL,
    expiracao TIMESTAMP NOT NULL,
    usado BOOLEAN DEFAULT FALSE,
    usado_em TIMESTAMP NULL,
    ip_geracao VARCHAR(45) NULL,
    user_agent TEXT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES auth_usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ===========================================
-- 10. TABELA OPCIONAL: sessoes_usuario
-- ===========================================
CREATE TABLE sessoes_usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    token_jwt VARCHAR(512) NOT NULL,
    ip_origem VARCHAR(45),
    user_agent TEXT,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expira_em TIMESTAMP NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (usuario_id) REFERENCES auth_usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_sessoes_usuario_token ON sessoes_usuario (token_jwt);

-- ===========================================
-- 11. TABELA OPCIONAL: historico_senhas
-- ===========================================
CREATE TABLE historico_senhas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES auth_usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_historico_usuario ON historico_senhas (usuario_id);

-- ===========================================
-- 12. TABELA OPCIONAL: logs_auditoria
-- ===========================================
CREATE TABLE logs_auditoria (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NULL,
    acao VARCHAR(255) NOT NULL,
    objeto_tipo VARCHAR(100) NULL,
    objeto_id BIGINT NULL,
    detalhes JSON NULL,
    ip_origem VARCHAR(45) NULL,
    user_agent TEXT NULL
);

CREATE INDEX idx_logs_auth_usuario ON logs_auditoria (usuario_id, timestamp);
