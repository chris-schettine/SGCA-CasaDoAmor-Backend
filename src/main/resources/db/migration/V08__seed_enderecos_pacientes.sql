-- =====================================================
-- Seed de endereços e pacientes para desenvolvimento/testes
-- V08__seed_enderecos_pacientes.sql
-- =====================================================

-- Inserir endereços de exemplo (apenas se não existirem)
INSERT INTO enderecos (id, logradouro, bairro, numero, cidade, estado, cep, complemento, created_at)
SELECT UUID(), 'Rua das Flores', 'Centro', 123, 'Vitória da Conquista', 'BAHIA', '45000-001', 'Apto 201', NOW()
WHERE NOT EXISTS (SELECT 1 FROM enderecos WHERE cep = '45000-001' AND numero = 123);

INSERT INTO enderecos (id, logradouro, bairro, numero, cidade, estado, cep, complemento, created_at)
SELECT UUID(), 'Av. Presidente Vargas', 'Brasil', 456, 'Vitória da Conquista', 'BAHIA', '45000-002', NULL, NOW()
WHERE NOT EXISTS (SELECT 1 FROM enderecos WHERE cep = '45000-002' AND numero = 456);

INSERT INTO enderecos (id, logradouro, bairro, numero, cidade, estado, cep, complemento, created_at)
SELECT UUID(), 'Rua da Paz', 'Candeias', 789, 'Vitória da Conquista', 'BAHIA', '45000-003', 'Casa', NOW()
WHERE NOT EXISTS (SELECT 1 FROM enderecos WHERE cep = '45000-003' AND numero = 789);

INSERT INTO enderecos (id, logradouro, bairro, numero, cidade, estado, cep, complemento, created_at)
SELECT UUID(), 'Av. Bartolomeu de Gusmão', 'Alto Maron', 321, 'Vitória da Conquista', 'BAHIA', '45000-004', NULL, NOW()
WHERE NOT EXISTS (SELECT 1 FROM enderecos WHERE cep = '45000-004' AND numero = 321);

INSERT INTO enderecos (id, logradouro, bairro, numero, cidade, estado, cep, complemento, created_at)
SELECT UUID(), 'Rua São Pedro', 'Recreio', 654, 'Vitória da Conquista', 'BAHIA', '45000-005', 'Fundos', NOW()
WHERE NOT EXISTS (SELECT 1 FROM enderecos WHERE cep = '45000-005' AND numero = 654);

INSERT INTO enderecos (id, logradouro, bairro, numero, cidade, estado, cep, complemento, created_at)
SELECT UUID(), 'Rua Juvenal Neves', 'Ibirapuera', 987, 'Vitória da Conquista', 'BAHIA', '45000-006', NULL, NOW()
WHERE NOT EXISTS (SELECT 1 FROM enderecos WHERE cep = '45000-006' AND numero = 987);

INSERT INTO enderecos (id, logradouro, bairro, numero, cidade, estado, cep, complemento, created_at)
SELECT UUID(), 'Av. Siqueira Campos', 'Guarani', 147, 'Vitória da Conquista', 'BAHIA', '45000-007', 'Bloco B', NOW()
WHERE NOT EXISTS (SELECT 1 FROM enderecos WHERE cep = '45000-007' AND numero = 147);

INSERT INTO enderecos (id, logradouro, bairro, numero, cidade, estado, cep, complemento, created_at)
SELECT UUID(), 'Rua Olinto Barata', 'Patagonia', 258, 'Vitória da Conquista', 'BAHIA', '45000-008', NULL, NOW()
WHERE NOT EXISTS (SELECT 1 FROM enderecos WHERE cep = '45000-008' AND numero = 258);

INSERT INTO enderecos (id, logradouro, bairro, numero, cidade, estado, cep, complemento, created_at)
SELECT UUID(), 'Rua José Pedral Sampaio', 'Felícia', 369, 'Vitória da Conquista', 'BAHIA', '45000-009', 'Casa 2', NOW()
WHERE NOT EXISTS (SELECT 1 FROM enderecos WHERE cep = '45000-009' AND numero = 369);

INSERT INTO enderecos (id, logradouro, bairro, numero, cidade, estado, cep, complemento, created_at)
SELECT UUID(), 'Rua Barão do Rio Branco', 'Jurema', 741, 'Vitória da Conquista', 'BAHIA', '45000-010', NULL, NOW()
WHERE NOT EXISTS (SELECT 1 FROM enderecos WHERE cep = '45000-010' AND numero = 741);

-- Criar pacientes associando dados_pessoais e endereços
-- Importante: Usamos subqueries para pegar os IDs corretos

-- Paciente 1: Maria da Silva Santos
INSERT INTO pacientes (id, dado_pessoal_id, endereco_id, created_at)
SELECT 
    UUID(),
    (SELECT id FROM dados_pessoais WHERE cpf = '12345678901'),
    (SELECT id FROM enderecos WHERE cep = '45000-001' AND numero = 123),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pacientes p 
    INNER JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id 
    WHERE dp.cpf = '12345678901'
);

-- Paciente 2: José Carlos Oliveira
INSERT INTO pacientes (id, dado_pessoal_id, endereco_id, created_at)
SELECT 
    UUID(),
    (SELECT id FROM dados_pessoais WHERE cpf = '23456789012'),
    (SELECT id FROM enderecos WHERE cep = '45000-002' AND numero = 456),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pacientes p 
    INNER JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id 
    WHERE dp.cpf = '23456789012'
);

-- Paciente 3: Ana Paula Rodrigues
INSERT INTO pacientes (id, dado_pessoal_id, endereco_id, created_at)
SELECT 
    UUID(),
    (SELECT id FROM dados_pessoais WHERE cpf = '34567890123'),
    (SELECT id FROM enderecos WHERE cep = '45000-003' AND numero = 789),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pacientes p 
    INNER JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id 
    WHERE dp.cpf = '34567890123'
);

-- Paciente 4: Pedro Henrique Costa
INSERT INTO pacientes (id, dado_pessoal_id, endereco_id, created_at)
SELECT 
    UUID(),
    (SELECT id FROM dados_pessoais WHERE cpf = '45678901234'),
    (SELECT id FROM enderecos WHERE cep = '45000-004' AND numero = 321),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pacientes p 
    INNER JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id 
    WHERE dp.cpf = '45678901234'
);

-- Paciente 5: Carla Fernanda Lima
INSERT INTO pacientes (id, dado_pessoal_id, endereco_id, created_at)
SELECT 
    UUID(),
    (SELECT id FROM dados_pessoais WHERE cpf = '56789012345'),
    (SELECT id FROM enderecos WHERE cep = '45000-005' AND numero = 654),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pacientes p 
    INNER JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id 
    WHERE dp.cpf = '56789012345'
);

-- Paciente 6: Roberto Santos Souza
INSERT INTO pacientes (id, dado_pessoal_id, endereco_id, created_at)
SELECT 
    UUID(),
    (SELECT id FROM dados_pessoais WHERE cpf = '67890123456'),
    (SELECT id FROM enderecos WHERE cep = '45000-006' AND numero = 987),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pacientes p 
    INNER JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id 
    WHERE dp.cpf = '67890123456'
);

-- Paciente 7: Fernanda Alves Pereira
INSERT INTO pacientes (id, dado_pessoal_id, endereco_id, created_at)
SELECT 
    UUID(),
    (SELECT id FROM dados_pessoais WHERE cpf = '78901234567'),
    (SELECT id FROM enderecos WHERE cep = '45000-007' AND numero = 147),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pacientes p 
    INNER JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id 
    WHERE dp.cpf = '78901234567'
);

-- Paciente 8: Antônio José Ferreira
INSERT INTO pacientes (id, dado_pessoal_id, endereco_id, created_at)
SELECT 
    UUID(),
    (SELECT id FROM dados_pessoais WHERE cpf = '89012345678'),
    (SELECT id FROM enderecos WHERE cep = '45000-008' AND numero = 258),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pacientes p 
    INNER JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id 
    WHERE dp.cpf = '89012345678'
);

-- Paciente 9: Juliana Mendes Almeida
INSERT INTO pacientes (id, dado_pessoal_id, endereco_id, created_at)
SELECT 
    UUID(),
    (SELECT id FROM dados_pessoais WHERE cpf = '90123456789'),
    (SELECT id FROM enderecos WHERE cep = '45000-009' AND numero = 369),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pacientes p 
    INNER JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id 
    WHERE dp.cpf = '90123456789'
);

-- Paciente 10: Marcos Paulo Silva
INSERT INTO pacientes (id, dado_pessoal_id, endereco_id, created_at)
SELECT 
    UUID(),
    (SELECT id FROM dados_pessoais WHERE cpf = '01234567890'),
    (SELECT id FROM enderecos WHERE cep = '45000-010' AND numero = 741),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pacientes p 
    INNER JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id 
    WHERE dp.cpf = '01234567890'
);

-- Comentário: Agora temos 10 pacientes completos com dados pessoais e endereços
