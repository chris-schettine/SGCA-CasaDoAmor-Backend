-- Script de Seed para SGCA Backend
-- Popula o banco de dados com dados de exemplo para testes

USE sgca;

-- ============================================
-- 1. DADOS PESSOAIS
-- ============================================

-- Admin
INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
VALUES 
('11111111-1111-1111-1111-111111111111', 'João Silva', '1980-05-15', '12345678901', '123456789', 'São Paulo', 'Administrador', '(11) 98765-4321', NOW()),
('22222222-2222-2222-2222-222222222222', 'Maria Santos', '1985-08-20', '98765432100', '987654321', 'Rio de Janeiro', 'Enfermeira', '(21) 91234-5678', NOW()),
('33333333-3333-3333-3333-333333333333', 'Carlos Oliveira', '1975-03-10', '11122233344', '111222333', 'Belo Horizonte', 'Médico', '(31) 99876-5432', NOW()),
('44444444-4444-4444-4444-444444444444', 'Ana Paula Costa', '1990-11-25', '55566677788', '555666777', 'Salvador', 'Psicóloga', '(71) 98888-7777', NOW());

-- Pacientes
INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
VALUES 
('55555555-5555-5555-5555-555555555555', 'José da Silva', '1945-06-30', '22233344455', '222333444', 'Recife', 'Aposentado', '(81) 97777-6666', NOW()),
('66666666-6666-6666-6666-666666666666', 'Francisca Pereira', '1950-12-15', '33344455566', '333444555', 'Fortaleza', 'Aposentada', '(85) 96666-5555', NOW());

-- Acompanhantes
INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, naturalidade, profissao, telefone, created_at)
VALUES 
('77777777-7777-7777-7777-777777777777', 'Pedro Silva', '1970-09-05', '44455566677', '444555666', 'Recife', 'Autônomo', '(81) 95555-4444', NOW()),
('88888888-8888-8888-8888-888888888888', 'Mariana Pereira', '1975-04-20', '66677788899', '666777888', 'Fortaleza', 'Do lar', '(85) 94444-3333', NOW());

-- ============================================
-- 2. ENDEREÇOS
-- ============================================

INSERT INTO enderecos (id, logradouro, bairro, numero, cidade, estado, cep, complemento, created_at)
VALUES 
('a1111111-1111-1111-1111-111111111111', 'Rua das Flores', 'Centro', 100, 'São Paulo', 'SP', '01000-000', 'Apto 101', NOW()),
('a2222222-2222-2222-2222-222222222222', 'Av. Atlântica', 'Copacabana', 500, 'Rio de Janeiro', 'RJ', '22000-000', 'Cobertura', NOW()),
('a3333333-3333-3333-3333-333333333333', 'Rua da Bahia', 'Savassi', 250, 'Belo Horizonte', 'MG', '30000-000', NULL, NOW()),
('a4444444-4444-4444-4444-444444444444', 'Av. Oceânica', 'Barra', 1500, 'Salvador', 'BA', '40000-000', 'Casa', NOW()),
('a5555555-5555-5555-5555-555555555555', 'Rua do Sol', 'Boa Viagem', 300, 'Recife', 'PE', '51000-000', 'Casa 1', NOW()),
('a6666666-6666-6666-6666-666666666666', 'Av. Beira Mar', 'Meireles', 800, 'Fortaleza', 'CE', '60000-000', 'Apto 202', NOW());

-- ============================================
-- 3. USUÁRIOS (Admin e Profissionais)
-- ============================================
-- Senha para todos: "senha123" (BCrypt com strength 12)
-- Hash gerado: $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYuP7VK1pHe

INSERT INTO usuarios (id, email, senha, ativo, tipo_usuario, dado_pessoal_id, endereco_id, created_at)
VALUES 
('u1111111-1111-1111-1111-111111111111', 'admin@casadoamor.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYuP7VK1pHe', TRUE, 'ADMIN', '11111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', NOW()),
('u2222222-2222-2222-2222-222222222222', 'enfermeira@casadoamor.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYuP7VK1pHe', TRUE, 'ENFERMEIRO', '22222222-2222-2222-2222-222222222222', 'a2222222-2222-2222-2222-222222222222', NOW()),
('u3333333-3333-3333-3333-333333333333', 'medico@casadoamor.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYuP7VK1pHe', TRUE, 'MEDICO', '33333333-3333-3333-3333-333333333333', 'a3333333-3333-3333-3333-333333333333', NOW()),
('u4444444-4444-4444-4444-444444444444', 'psicologa@casadoamor.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYuP7VK1pHe', TRUE, 'PSICOLOGO', '44444444-4444-4444-4444-444444444444', 'a4444444-4444-4444-4444-444444444444', NOW()),
-- Email de teste com o Gmail configurado
('u5555555-5555-5555-5555-555555555555', 'casadoamoremconquista@gmail.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYuP7VK1pHe', TRUE, 'ADMIN', '11111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', NOW()),
-- Email do desenvolvedor para testes
('u6666666-6666-6666-6666-666666666666', 'j.henrique.uesb@gmail.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYuP7VK1pHe', TRUE, 'ADMIN', '11111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', NOW());

-- ============================================
-- 4. PROFISSIONAIS DE SAÚDE
-- ============================================

INSERT INTO profissional_saude (id, email, senha, ativo, tipo, documento, uf_documento, especialidade, dado_pessoal_id, endereco_id, created_at)
VALUES 
('p2222222-2222-2222-2222-222222222222', 'enfermeira@casadoamor.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYuP7VK1pHe', TRUE, 'ENFERMEIRO', 'COREN-123456', 'RJ', 'Enfermagem Geral', '22222222-2222-2222-2222-222222222222', 'a2222222-2222-2222-2222-222222222222', NOW()),
('p3333333-3333-3333-3333-333333333333', 'medico@casadoamor.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYuP7VK1pHe', TRUE, 'MEDICO', 'CRM-654321', 'MG', 'Clínica Geral', '33333333-3333-3333-3333-333333333333', 'a3333333-3333-3333-3333-333333333333', NOW()),
('p4444444-4444-4444-4444-444444444444', 'psicologa@casadoamor.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYuP7VK1pHe', TRUE, 'PSICOLOGO', 'CRP-789012', 'BA', 'Psicologia Clínica', '44444444-4444-4444-4444-444444444444', 'a4444444-4444-4444-4444-444444444444', NOW());

-- ============================================
-- 5. PACIENTES
-- ============================================

INSERT INTO pacientes (id, dado_pessoal_id, endereco_id, created_at)
VALUES 
('pac55555-5555-5555-5555-555555555555', '55555555-5555-5555-5555-555555555555', 'a5555555-5555-5555-5555-555555555555', NOW()),
('pac66666-6666-6666-6666-666666666666', '66666666-6666-6666-6666-666666666666', 'a6666666-6666-6666-6666-666666666666', NOW());

-- ============================================
-- 6. DADOS CLÍNICOS
-- ============================================

INSERT INTO dados_clinicos (id, diagnostico, tratamento, usa_sonda, tipo_sonda, usa_curativo, paciente_id, created_at)
VALUES 
('dc555555-5555-5555-5555-555555555555', 'Diabetes Mellitus Tipo 2', 'Insulinoterapia e dieta controlada', TRUE, 'Sonda Nasoenteral', TRUE, 'pac55555-5555-5555-5555-555555555555', NOW()),
('dc666666-6666-6666-6666-666666666666', 'Hipertensão Arterial', 'Controle medicamentoso', FALSE, NULL, FALSE, 'pac66666-6666-6666-6666-666666666666', NOW());

-- ============================================
-- 7. ACOMPANHANTES
-- ============================================

INSERT INTO acompanhantes (id, pode_ajudar_na_cozinha, dado_pessoal_id, endereco_id, ativo, created_at)
VALUES 
('acomp777-7777-7777-7777-777777777777', TRUE, '77777777-7777-7777-7777-777777777777', 'a5555555-5555-5555-5555-555555555555', TRUE, NOW()),
('acomp888-8888-8888-8888-888888888888', TRUE, '88888888-8888-8888-8888-888888888888', 'a6666666-6666-6666-6666-666666666666', TRUE, NOW());

-- ============================================
-- 8. RESUMO DOS DADOS INSERIDOS
-- ============================================

SELECT '=== RESUMO DO SEED ===' as '';
SELECT CONCAT('Usuários inseridos: ', COUNT(*)) as '' FROM usuarios;
SELECT CONCAT('Profissionais de saúde inseridos: ', COUNT(*)) as '' FROM profissional_saude;
SELECT CONCAT('Pacientes inseridos: ', COUNT(*)) as '' FROM pacientes;
SELECT CONCAT('Acompanhantes inseridos: ', COUNT(*)) as '' FROM acompanhantes;
SELECT CONCAT('Dados pessoais inseridos: ', COUNT(*)) as '' FROM dados_pessoais;
SELECT CONCAT('Endereços inseridos: ', COUNT(*)) as '' FROM enderecos;

SELECT '=== CREDENCIAIS DE TESTE ===' as '';
SELECT 'Email: admin@casadoamor.com | Senha: senha123' as 'Admin';
SELECT 'Email: enfermeira@casadoamor.com | Senha: senha123' as 'Enfermeira';
SELECT 'Email: medico@casadoamor.com | Senha: senha123' as 'Médico';
SELECT 'Email: psicologa@casadoamor.com | Senha: senha123' as 'Psicóloga';
SELECT 'Email: casadoamoremconquista@gmail.com | Senha: senha123' as 'Admin (Gmail)';
SELECT 'Email: j.henrique.uesb@gmail.com | Senha: senha123' as 'Desenvolvedor (Teste Email)';
SELECT '' as '';
SELECT 'Seed executado com sucesso!' as 'Status';
