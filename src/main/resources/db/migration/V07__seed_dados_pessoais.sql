-- =====================================================
-- Seed de dados pessoais para desenvolvimento/testes
-- V07__seed_dados_pessoais.sql
-- =====================================================

-- Inserir dados pessoais de exemplo (apenas se não existirem)
-- Usando INSERT IGNORE para evitar duplicatas por CPF único

INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
SELECT UUID(), 'Maria da Silva Santos', '1985-03-15', '12345678901', '1234567', 'Vitória da Conquista - BA', 'Professora', '(77) 98765-4321', NOW()
WHERE NOT EXISTS (SELECT 1 FROM dados_pessoais WHERE cpf = '12345678901');

INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
SELECT UUID(), 'José Carlos Oliveira', '1978-07-22', '23456789012', '2345678', 'Itapetinga - BA', 'Comerciante', '(77) 99876-5432', NOW()
WHERE NOT EXISTS (SELECT 1 FROM dados_pessoais WHERE cpf = '23456789012');

INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
SELECT UUID(), 'Ana Paula Rodrigues', '1990-11-08', '34567890123', '3456789', 'Guanambi - BA', 'Enfermeira', '(77) 98888-1111', NOW()
WHERE NOT EXISTS (SELECT 1 FROM dados_pessoais WHERE cpf = '34567890123');

INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
SELECT UUID(), 'Pedro Henrique Costa', '1965-05-30', '45678901234', '4567890', 'Brumado - BA', 'Aposentado', '(77) 99777-2222', NOW()
WHERE NOT EXISTS (SELECT 1 FROM dados_pessoais WHERE cpf = '45678901234');

INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
SELECT UUID(), 'Carla Fernanda Lima', '1982-09-12', '56789012345', '5678901', 'Jequié - BA', 'Autônoma', '(77) 98666-3333', NOW()
WHERE NOT EXISTS (SELECT 1 FROM dados_pessoais WHERE cpf = '56789012345');

INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
SELECT UUID(), 'Roberto Santos Souza', '1975-01-25', '67890123456', '6789012', 'Vitória da Conquista - BA', 'Motorista', '(77) 99555-4444', NOW()
WHERE NOT EXISTS (SELECT 1 FROM dados_pessoais WHERE cpf = '67890123456');

INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
SELECT UUID(), 'Fernanda Alves Pereira', '1988-12-03', '78901234567', '7890123', 'Poções - BA', 'Administradora', '(77) 98444-5555', NOW()
WHERE NOT EXISTS (SELECT 1 FROM dados_pessoais WHERE cpf = '78901234567');

INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
SELECT UUID(), 'Antônio José Ferreira', '1970-06-18', '89012345678', '8901234', 'Cândido Sales - BA', 'Agricultor', '(77) 99333-6666', NOW()
WHERE NOT EXISTS (SELECT 1 FROM dados_pessoais WHERE cpf = '89012345678');

INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
SELECT UUID(), 'Juliana Mendes Almeida', '1995-04-07', '90123456789', '9012345', 'Vitória da Conquista - BA', 'Estudante', '(77) 98222-7777', NOW()
WHERE NOT EXISTS (SELECT 1 FROM dados_pessoais WHERE cpf = '90123456789');

INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
SELECT UUID(), 'Marcos Paulo Silva', '1980-08-14', '01234567890', '0123456', 'Caetité - BA', 'Técnico em Informática', '(77) 99111-8888', NOW()
WHERE NOT EXISTS (SELECT 1 FROM dados_pessoais WHERE cpf = '01234567890');

-- Comentário: Estes dados podem ser usados para criar pacientes de teste
-- Relacionando com tabelas de endereços e pacientes conforme necessário
