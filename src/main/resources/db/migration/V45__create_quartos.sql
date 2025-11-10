-- Criação da tabela de quartos/leitos
-- Gerenciamento de acomodações da instituição

CREATE TABLE quartos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE COMMENT 'Identificador único UUID',
    
    -- Dados do Quarto
    nome VARCHAR(100) NOT NULL COMMENT 'Nome/número do quarto (ex: Quarto 101, Ala A - Leito 3)',
    codigo VARCHAR(50) UNIQUE COMMENT 'Código único do quarto',
    tipo ENUM('INDIVIDUAL', 'COMPARTILHADO') NOT NULL DEFAULT 'COMPARTILHADO' COMMENT 'Tipo de quarto',
    ala ENUM('FEMININA', 'MASCULINA', 'MISTA') NOT NULL COMMENT 'Ala do quarto (separação por gênero)',
    andar VARCHAR(20) COMMENT 'Andar/pavimento (ex: Térreo, 1º andar)',
    
    -- Capacidade
    capacidade_total INT NOT NULL DEFAULT 1 COMMENT 'Capacidade total de leitos',
    capacidade_ocupada INT NOT NULL DEFAULT 0 COMMENT 'Quantidade de leitos ocupados atualmente',
    
    -- Status e Observações
    ativo BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Quarto disponível para uso',
    em_manutencao BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Quarto temporariamente indisponível',
    observacoes TEXT COMMENT 'Observações sobre o quarto',
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'Usuário que cadastrou',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT COMMENT 'Usuário que atualizou',
    deleted_at TIMESTAMP NULL COMMENT 'Soft delete',
    
    -- Foreign Keys
    CONSTRAINT fk_quartos_created_by FOREIGN KEY (created_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_quartos_updated_by FOREIGN KEY (updated_by) REFERENCES auth_usuarios(id) ON DELETE SET NULL,
    
    -- Validações
    CONSTRAINT chk_quartos_capacidade CHECK (capacidade_total > 0),
    CONSTRAINT chk_quartos_ocupacao CHECK (capacidade_ocupada >= 0 AND capacidade_ocupada <= capacidade_total),
    
    -- Índices
    INDEX idx_quartos_uuid (uuid),
    INDEX idx_quartos_codigo (codigo),
    INDEX idx_quartos_ala (ala),
    INDEX idx_quartos_tipo (tipo),
    INDEX idx_quartos_ativo (ativo),
    INDEX idx_quartos_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
COMMENT='Quartos e leitos disponíveis na instituição';
