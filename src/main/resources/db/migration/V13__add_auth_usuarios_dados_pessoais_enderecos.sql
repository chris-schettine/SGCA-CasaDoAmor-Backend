-- ===========================================
--  ADICIONA DADOS PESSOAIS E ENDEREÇOS PARA USUÁRIOS DO SISTEMA
--  Versão: 1.0
--  Data: 2025-10-23
--  Descrição: Cria tabelas separadas para dados pessoais e endereços
--             de usuários do sistema (funcionários), mantendo separação
--             de contexto com dados de pacientes
-- ===========================================

-- ===========================================
-- 1. TABELA: auth_usuarios_enderecos
-- ===========================================
CREATE TABLE auth_usuarios_enderecos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    logradouro VARCHAR(150) NOT NULL,
    numero VARCHAR(10),
    complemento VARCHAR(150),
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    uf CHAR(2) NOT NULL,
    cep VARCHAR(9),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por BIGINT NULL,
    atualizado_em TIMESTAMP NULL,
    atualizado_por BIGINT NULL,
    deletado_em TIMESTAMP NULL,
    
    FOREIGN KEY (criado_por) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (atualizado_por) REFERENCES auth_usuarios(id) ON DELETE SET NULL
);

CREATE INDEX idx_auth_usuarios_enderecos_cep ON auth_usuarios_enderecos(cep);
CREATE INDEX idx_auth_usuarios_enderecos_cidade_uf ON auth_usuarios_enderecos(cidade, uf);

-- ===========================================
-- 2. TABELA: auth_usuarios_dados_pessoais
-- ===========================================
CREATE TABLE auth_usuarios_dados_pessoais (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    data_nascimento DATE,
    sexo ENUM('MASCULINO', 'FEMININO') NOT NULL,
    genero ENUM('BINARIO', 'NAO_BINARIO', 'TRANSGENERO', 'CISGENERO', 'PREFIRO_NAO_INFORMAR') NOT NULL DEFAULT 'PREFIRO_NAO_INFORMAR',
    rg VARCHAR(20),
    orgao_emissor VARCHAR(10),
    naturalidade VARCHAR(100),
    estado_civil ENUM('SOLTEIRO', 'CASADO', 'DIVORCIADO', 'VIUVO', 'UNIAO_ESTAVEL') NULL,
    nome_mae VARCHAR(255),
    nome_pai VARCHAR(255),
    profissao VARCHAR(100),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por BIGINT NULL,
    atualizado_em TIMESTAMP NULL,
    atualizado_por BIGINT NULL,
    deletado_em TIMESTAMP NULL,
    
    FOREIGN KEY (criado_por) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (atualizado_por) REFERENCES auth_usuarios(id) ON DELETE SET NULL
);

-- ===========================================
-- 3. ADICIONA FOREIGN KEYS EM auth_usuarios
-- ===========================================
ALTER TABLE auth_usuarios 
ADD COLUMN endereco_id BIGINT NULL,
ADD COLUMN dados_pessoais_id BIGINT NULL;

ALTER TABLE auth_usuarios
ADD CONSTRAINT fk_auth_usuarios_endereco 
    FOREIGN KEY (endereco_id) REFERENCES auth_usuarios_enderecos(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_auth_usuarios_dados_pessoais 
    FOREIGN KEY (dados_pessoais_id) REFERENCES auth_usuarios_dados_pessoais(id) ON DELETE SET NULL;

CREATE INDEX idx_auth_usuarios_endereco ON auth_usuarios(endereco_id);
CREATE INDEX idx_auth_usuarios_dados_pessoais ON auth_usuarios(dados_pessoais_id);

-- ===========================================
-- 4. SEED - CRIAR DADOS PESSOAIS PARA USUÁRIOS EXISTENTES
-- ===========================================

-- Inserir dados pessoais padrão para usuário admin (00000000001)
INSERT INTO auth_usuarios_dados_pessoais (sexo, genero, criado_por)
SELECT 
    'MASCULINO' as sexo,
    'PREFIRO_NAO_INFORMAR' as genero,
    id as criado_por
FROM auth_usuarios 
WHERE cpf = '00000000001'
LIMIT 1;

-- Associar dados pessoais ao usuário admin
UPDATE auth_usuarios au
SET dados_pessoais_id = (
    SELECT id 
    FROM auth_usuarios_dados_pessoais 
    WHERE criado_por = au.id
    LIMIT 1
)
WHERE cpf = '00000000001';

-- Inserir dados pessoais padrão para usuário recepcionista de teste (se existir)
INSERT INTO auth_usuarios_dados_pessoais (sexo, genero, criado_por)
SELECT 
    'MASCULINO' as sexo,
    'PREFIRO_NAO_INFORMAR' as genero,
    id as criado_por
FROM auth_usuarios 
WHERE email = 'southhenrique@hotmail.com'
LIMIT 1;

-- Associar dados pessoais ao usuário recepcionista
UPDATE auth_usuarios au
SET dados_pessoais_id = (
    SELECT id 
    FROM auth_usuarios_dados_pessoais 
    WHERE criado_por = au.id
    AND au.email = 'southhenrique@hotmail.com'
    LIMIT 1
)
WHERE email = 'southhenrique@hotmail.com';

-- ===========================================
-- 5. COMENTÁRIOS DAS TABELAS
-- ===========================================
ALTER TABLE auth_usuarios_enderecos 
COMMENT = 'Endereços de usuários do sistema (funcionários) - separado de endereços de pacientes';

ALTER TABLE auth_usuarios_dados_pessoais 
COMMENT = 'Dados pessoais de usuários do sistema (funcionários) - separado de dados pessoais de pacientes';
