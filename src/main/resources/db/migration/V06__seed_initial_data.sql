-- =====================================================
-- Seed inicial para desenvolvimento/testes
-- V06__seed_initial_data.sql
-- =====================================================

-- Inserir admin padrão (se não existir)
-- Senha: Admin@123 (BCrypt hash with strength 12)
INSERT INTO auth_usuarios (uuid, nome, email, cpf, senha_hash, telefone, tipo, ativo, email_verificado, criado_em)
SELECT UUID(), 'Admin Sistema', 'admin@casadoamor.com', '00000000000', 
       '$2a$12$qbIC3NA71l2f5cGMhRrIKO.53MWP5o7I12gdYfqI.Mt/CG6MlvBCG', 
       '(77) 99999-9999', 'ADMINISTRADOR', true, true, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM auth_usuarios WHERE cpf = '00000000000'
);

-- Inserir permissões básicas (apenas se não existirem)
INSERT IGNORE INTO permissoes (nome, descricao, criado_em) VALUES
('PACIENTE_READ', 'Permissão para visualizar pacientes', NOW()),
('PACIENTE_WRITE', 'Permissão para criar e editar pacientes', NOW()),
('PACIENTE_DELETE', 'Permissão para excluir pacientes', NOW()),
('PRONTUARIO_READ', 'Permissão para visualizar prontuários', NOW()),
('PRONTUARIO_WRITE', 'Permissão para criar e editar prontuários', NOW()),
('PRONTUARIO_DELETE', 'Permissão para excluir prontuários', NOW()),
('USER_READ', 'Permissão para visualizar usuários', NOW()),
('USER_WRITE', 'Permissão para criar e editar usuários', NOW()),
('USER_DELETE', 'Permissão para excluir usuários', NOW());

-- Inserir perfis básicos (apenas se não existirem)
INSERT IGNORE INTO perfis (nome, descricao, criado_em) VALUES
('MEDICO_GERAL', 'Perfil para médicos com acesso geral', NOW()),
('ENFERMEIRO', 'Perfil para enfermeiros', NOW()),
('RECEPCIONISTA', 'Perfil para recepcionistas', NOW()),
('PSICOLOGO', 'Perfil para psicólogos', NOW());

-- Associar permissões aos perfis
-- MEDICO_GERAL: acesso completo a pacientes e prontuários
INSERT IGNORE INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'MEDICO_GERAL' 
  AND pm.nome IN ('PACIENTE_READ', 'PACIENTE_WRITE', 'PRONTUARIO_READ', 'PRONTUARIO_WRITE');

-- ENFERMEIRO: leitura de pacientes e prontuários
INSERT IGNORE INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'ENFERMEIRO' 
  AND pm.nome IN ('PACIENTE_READ', 'PRONTUARIO_READ');

-- RECEPCIONISTA: gestão de pacientes
INSERT IGNORE INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'RECEPCIONISTA' 
  AND pm.nome IN ('PACIENTE_READ', 'PACIENTE_WRITE');

-- PSICOLOGO: acesso completo a pacientes e prontuários
INSERT IGNORE INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'PSICOLOGO' 
  AND pm.nome IN ('PACIENTE_READ', 'PACIENTE_WRITE', 'PRONTUARIO_READ', 'PRONTUARIO_WRITE');

-- Comentário: Para adicionar mais dados de seed, adicione aqui
-- Exemplo de usuário de teste (descomente se necessário):
/*
INSERT INTO auth_usuarios (uuid, nome, email, cpf, senha_hash, telefone, tipo, ativo, email_verificado, criado_em)
SELECT UUID(), 'Dr. João Silva', 'joao.silva@casadoamor.com', '12345678900',
       '$2a$12$sT5Kl3uEbERZKnTDB0PDT.flDPyuJLSEcZ5tnMksInE7p0389HcNm',
       '(77) 98888-7777', 'MEDICO', true, true, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM auth_usuarios WHERE cpf = '12345678900'
);
*/
