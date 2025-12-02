-- V55: Criar tabela tipos_vinculo e popular com dados padrão
-- Sincronização com banco de dados da nuvem

-- Criar tabela tipos_vinculo
CREATE TABLE IF NOT EXISTS tipos_vinculo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL UNIQUE COMMENT 'Código identificador do tipo de vínculo',
    nome VARCHAR(100) NOT NULL COMMENT 'Nome/descrição do tipo de vínculo',
    ativo BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Indica se o tipo está ativo',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data de criação do registro',
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Data da última atualização'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tipos de vínculo empregatício dos profissionais';

-- Popular com dados padrão (usar INSERT IGNORE para evitar erros se dados já existirem)
INSERT IGNORE INTO tipos_vinculo (id, codigo, nome, ativo, created_at) VALUES
(1, 'PADRAO', 'Padrão', TRUE, '2025-11-10 23:12:14'),
(2, 'CLT', 'CLT', TRUE, '2025-11-10 23:12:14'),
(3, 'PJ', 'Pessoa Jurídica', TRUE, '2025-11-10 23:12:14'),
(4, 'AUT', 'Autônomo', TRUE, '2025-11-10 23:12:14'),
(5, 'VOL', 'Voluntário', TRUE, '2025-11-10 23:12:14');

-- Criar índice para performance
CREATE INDEX idx_tipos_vinculo_ativo ON tipos_vinculo(ativo);
CREATE INDEX idx_tipos_vinculo_codigo ON tipos_vinculo(codigo);
