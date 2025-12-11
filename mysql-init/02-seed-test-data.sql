-- =====================================================
-- Script de População de Dados de Teste - SGCA
-- =====================================================
-- FOCO: Dados essenciais para testes de agendamentos
-- =====================================================

USE sgca;

SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;
SET AUTOCOMMIT = 0;

-- =====================================================
-- 1. REGISTRO PROFISSIONAL DO MÉDICO
-- =====================================================

-- Verificar se já existe o registro profissional
INSERT IGNORE INTO auth_usuarios_registros_profissionais (usuario_id, tipo_profissional, numero_registro, rqe, criado_em, criado_por) VALUES
(2, 'MEDICO', 'CRM-SP-123456', 'RQE-12345', NOW(), 2);

-- =====================================================
-- 2. PERFIL DO MÉDICO
-- =====================================================

-- Associar perfil MEDICO ao usuário
INSERT IGNORE INTO auth_usuarios_perfis (usuario_id, perfil_id) 
SELECT 2, id FROM perfis WHERE nome = 'MEDICO' LIMIT 1;

-- =====================================================
-- 3. HORÁRIOS DE ATENDIMENTO DO MÉDICO
-- =====================================================

-- Limpar horários existentes do médico (se houver)
DELETE FROM horarios_profissionais WHERE profissional_usuario_id = 2;

-- Segunda-feira: 08:00-12:00 e 14:00-18:00
INSERT INTO horarios_profissionais (profissional_usuario_id, dia_semana, hora_inicio, hora_fim, ativo, observacoes, created_at) VALUES
(2, 1, '08:00:00', '12:00:00', 1, 'Atendimento matutino', NOW()),
(2, 1, '14:00:00', '18:00:00', 1, 'Atendimento vespertino', NOW());

-- Terça-feira: 08:00-12:00 e 14:00-18:00
INSERT INTO horarios_profissionais (profissional_usuario_id, dia_semana, hora_inicio, hora_fim, ativo, observacoes, created_at) VALUES
(2, 2, '08:00:00', '12:00:00', 1, 'Atendimento matutino', NOW()),
(2, 2, '14:00:00', '18:00:00', 1, 'Atendimento vespertino', NOW());

-- Quarta-feira: 08:00-12:00 e 14:00-18:00
INSERT INTO horarios_profissionais (profissional_usuario_id, dia_semana, hora_inicio, hora_fim, ativo, observacoes, created_at) VALUES
(2, 3, '08:00:00', '12:00:00', 1, 'Atendimento matutino', NOW()),
(2, 3, '14:00:00', '18:00:00', 1, 'Atendimento vespertino', NOW());

-- Quinta-feira: 08:00-12:00 e 14:00-18:00
INSERT INTO horarios_profissionais (profissional_usuario_id, dia_semana, hora_inicio, hora_fim, ativo, observacoes, created_at) VALUES
(2, 4, '08:00:00', '12:00:00', 1, 'Atendimento matutino', NOW()),
(2, 4, '14:00:00', '18:00:00', 1, 'Atendimento vespertino', NOW());

-- Sexta-feira: 08:00-12:00 (meio período)
INSERT INTO horarios_profissionais (profissional_usuario_id, dia_semana, hora_inicio, hora_fim, ativo, observacoes, created_at) VALUES
(2, 5, '08:00:00', '12:00:00', 1, 'Atendimento matutino - meio período', NOW());

-- =====================================================
-- 4. DADOS PESSOAIS PARA PACIENTES
-- =====================================================

INSERT INTO dados_pessoais (id, nome, data_nascimento, cpf, rg, sexo, estado_civil, profissao, telefone, created_at) VALUES
(UUID(), 'Maria Silva Santos', '1945-03-15', '12345678901', '123456789', 'FEMININO', 'VIUVO', 'Aposentada', '(11) 98765-4321', NOW()),
(UUID(), 'João Pedro Oliveira', '1950-07-22', '98765432100', '987654321', 'MASCULINO', 'CASADO', 'Aposentado', '(11) 97654-3210', NOW()),
(UUID(), 'Ana Carolina Souza', '1948-11-30', '45678912300', '456789123', 'FEMININO', 'CASADO', 'Professora Aposentada', '(11) 96543-2109', NOW()),
(UUID(), 'Carlos Eduardo Lima', '1952-05-18', '78912345600', '789123456', 'MASCULINO', 'DIVORCIADO', 'Comerciante', '(11) 95432-1098', NOW()),
(UUID(), 'Rosa Maria Costa', '1943-09-25', '32165498700', '321654987', 'FEMININO', 'VIUVO', 'Do lar', '(11) 94321-0987', NOW());

-- =====================================================
-- 5. ENDEREÇOS PARA PACIENTES
-- =====================================================

INSERT INTO enderecos (id, cep, logradouro, numero, complemento, bairro, cidade, estado, created_at) VALUES
(UUID(), '01310-100', 'Av. Paulista', 1000, 'Apto 501', 'Bela Vista', 'São Paulo', 'SP', NOW()),
(UUID(), '04101-300', 'Rua Vergueiro', 2500, 'Casa', 'Vila Mariana', 'São Paulo', 'SP', NOW()),
(UUID(), '05402-000', 'Av. Pedroso de Morais', 800, 'Apto 102', 'Pinheiros', 'São Paulo', 'SP', NOW()),
(UUID(), '03310-000', 'Rua da Mooca', 1500, 'Casa', 'Mooca', 'São Paulo', 'SP', NOW()),
(UUID(), '02012-001', 'Av. Cruzeiro do Sul', 3200, 'Apto 803', 'Santana', 'São Paulo', 'SP', NOW());

-- =====================================================
-- 6. PACIENTES
-- =====================================================

INSERT INTO pacientes (id, dado_pessoal_id, endereco_id, status, observacoes, created_at, created_by) VALUES
(UUID(), (SELECT id FROM dados_pessoais WHERE cpf = '12345678901'), (SELECT id FROM enderecos WHERE numero = 1000 AND logradouro = 'Av. Paulista'), 'ATIVO', 'Paciente diagnosticada com câncer de mama. Em tratamento quimioterápico.', NOW(), 'SYSTEM'),
(UUID(), (SELECT id FROM dados_pessoais WHERE cpf = '98765432100'), (SELECT id FROM enderecos WHERE numero = 2500 AND logradouro = 'Rua Vergueiro'), 'ATIVO', 'Paciente com câncer de próstata. Realizou cirurgia recentemente.', NOW(), 'SYSTEM'),
(UUID(), (SELECT id FROM dados_pessoais WHERE cpf = '45678912300'), (SELECT id FROM enderecos WHERE numero = 800 AND logradouro = 'Av. Pedroso de Morais'), 'ATIVO', 'Paciente com câncer de cólon. Iniciou tratamento há 3 meses.', NOW(), 'SYSTEM'),
(UUID(), (SELECT id FROM dados_pessoais WHERE cpf = '78912345600'), (SELECT id FROM enderecos WHERE numero = 1500 AND logradouro = 'Rua da Mooca'), 'ATIVO', 'Paciente com câncer de pulmão. Necessita acompanhamento constante.', NOW(), 'SYSTEM'),
(UUID(), (SELECT id FROM dados_pessoais WHERE cpf = '32165498700'), (SELECT id FROM enderecos WHERE numero = 3200 AND logradouro = 'Av. Cruzeiro do Sul'), 'ATIVO', 'Paciente com câncer de pele. Em fase inicial de tratamento.', NOW(), 'SYSTEM');

-- =====================================================
-- COMMIT
-- =====================================================

SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS = 1;
COMMIT;

-- =====================================================
-- RESUMO DOS DADOS INSERIDOS
-- =====================================================
-- 1 Registro Profissional (médico CRM-SP-123456)
-- 1 Perfil de Usuário (médico)
-- 9 Horários de Atendimento (seg a sex)
-- 5 Dados Pessoais
-- 5 Endereços
-- 5 Pacientes
-- =====================================================
