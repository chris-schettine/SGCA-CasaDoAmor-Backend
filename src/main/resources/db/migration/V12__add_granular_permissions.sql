-- =====================================================
-- Migration V12: Adiciona Permissões Granulares
-- Descrição: Adiciona permissões específicas para controle
--            de acesso detalhado ao sistema
-- =====================================================

-- Limpar permissões antigas genéricas (se existirem)
DELETE FROM perfis_permissoes;
DELETE FROM permissoes;

-- Inserir novas permissões granulares
INSERT INTO permissoes (nome, descricao, criado_em) VALUES
-- Gerenciamento de Usuários
('USUARIOS_CRIAR', 'Permissão para criar novos usuários no sistema', NOW()),
('USUARIOS_EDITAR', 'Permissão para editar dados de usuários existentes', NOW()),
('USUARIOS_EXCLUIR', 'Permissão para excluir usuários do sistema', NOW()),
('USUARIOS_VER', 'Permissão para visualizar lista e detalhes de usuários', NOW()),

-- Gerenciamento de Perfis (Roles)
('ROLES_CRIAR', 'Permissão para criar novos perfis de acesso', NOW()),
('ROLES_EDITAR', 'Permissão para editar perfis existentes', NOW()),
('ROLES_EXCLUIR', 'Permissão para excluir perfis de acesso', NOW()),
('ROLES_VER', 'Permissão para visualizar perfis de acesso', NOW()),

-- Gerenciamento de Permissões
('PERMISSOES_CRIAR', 'Permissão para criar novas permissões', NOW()),
('PERMISSOES_EDITAR', 'Permissão para editar permissões existentes', NOW()),
('PERMISSOES_EXCLUIR', 'Permissão para excluir permissões', NOW()),
('PERMISSOES_VER', 'Permissão para visualizar permissões', NOW()),

-- Gerenciamento de Áreas do Sistema
('AREAS_CRIAR', 'Permissão para criar novas áreas do sistema', NOW()),
('AREAS_EDITAR', 'Permissão para editar áreas do sistema', NOW()),
('AREAS_EXCLUIR', 'Permissão para excluir áreas do sistema', NOW()),
('AREAS_VER', 'Permissão para visualizar áreas do sistema', NOW()),

-- Auditoria e Relatórios
('GERAR_RELATORIO_LOGIN', 'Permissão para gerar relatórios de tentativas de login', NOW()),
('VER_LOG_AUDITORIA', 'Permissão para visualizar logs de auditoria do sistema', NOW()),

-- Ações Administrativas Especiais
('FORCAR_LOGOUT', 'Permissão para forçar logout de usuários (revogar sessões)', NOW()),
('GERENCIAR_2FA', 'Permissão para habilitar/desabilitar 2FA de usuários', NOW()),
('GERENCIAR_SENHAS', 'Permissão para resetar senhas de usuários via administração', NOW()),

-- Gerenciamento de Pacientes
('PACIENTES_CRIAR', 'Permissão para cadastrar novos pacientes', NOW()),
('PACIENTES_EDITAR', 'Permissão para editar dados de pacientes', NOW()),
('PACIENTES_EXCLUIR', 'Permissão para excluir pacientes', NOW()),
('PACIENTES_VER', 'Permissão para visualizar dados de pacientes', NOW()),

-- Gerenciamento de Dados Clínicos
('DADOS_CLINICOS_CRIAR', 'Permissão para criar registros de dados clínicos', NOW()),
('DADOS_CLINICOS_EDITAR', 'Permissão para editar dados clínicos', NOW()),
('DADOS_CLINICOS_EXCLUIR', 'Permissão para excluir dados clínicos', NOW()),
('DADOS_CLINICOS_VER', 'Permissão para visualizar dados clínicos', NOW());

-- =====================================================
-- Atribuir Permissões aos Perfis
-- =====================================================

-- PERFIL: ADMINISTRADOR (acesso completo)
INSERT INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'ADMINISTRADOR';

-- PERFIL: COORDENADOR (sem excluir usuários, com gerenciamento operacional)
INSERT INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'COORDENADOR'
  AND pm.nome IN (
    'USUARIOS_VER', 'USUARIOS_EDITAR',
    'ROLES_VER',
    'PACIENTES_CRIAR', 'PACIENTES_EDITAR', 'PACIENTES_VER',
    'DADOS_CLINICOS_CRIAR', 'DADOS_CLINICOS_EDITAR', 'DADOS_CLINICOS_VER',
    'GERAR_RELATORIO_LOGIN', 'VER_LOG_AUDITORIA'
  );

-- PERFIL: MEDICO (acesso a pacientes e dados clínicos completos)
INSERT INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'MEDICO_GERAL'
  AND pm.nome IN (
    'PACIENTES_VER', 'PACIENTES_EDITAR',
    'DADOS_CLINICOS_CRIAR', 'DADOS_CLINICOS_EDITAR', 'DADOS_CLINICOS_VER'
  );

-- PERFIL: ENFERMEIRO (acesso a pacientes e dados clínicos)
INSERT INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'ENFERMEIRO'
  AND pm.nome IN (
    'PACIENTES_VER',
    'DADOS_CLINICOS_CRIAR', 'DADOS_CLINICOS_EDITAR', 'DADOS_CLINICOS_VER'
  );

-- PERFIL: RECEPCIONISTA (gestão de pacientes, sem dados clínicos)
INSERT INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'RECEPCIONISTA'
  AND pm.nome IN (
    'PACIENTES_CRIAR', 'PACIENTES_EDITAR', 'PACIENTES_VER'
  );

-- PERFIL: PSICOLOGO (acesso a pacientes e dados clínicos)
INSERT INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'PSICOLOGO'
  AND pm.nome IN (
    'PACIENTES_VER', 'PACIENTES_EDITAR',
    'DADOS_CLINICOS_CRIAR', 'DADOS_CLINICOS_EDITAR', 'DADOS_CLINICOS_VER'
  );

-- PERFIL: AUDITOR (somente leitura de logs e relatórios)
-- Criar perfil AUDITOR se não existir
INSERT INTO perfis (nome, descricao, criado_em)
SELECT 'AUDITOR', 'Perfil para auditores com acesso a logs e relatórios', NOW()
WHERE NOT EXISTS (SELECT 1 FROM perfis WHERE nome = 'AUDITOR');

INSERT INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'AUDITOR'
  AND pm.nome IN (
    'GERAR_RELATORIO_LOGIN',
    'VER_LOG_AUDITORIA',
    'USUARIOS_VER',
    'PACIENTES_VER',
    'DADOS_CLINICOS_VER'
  );

-- =====================================================
-- Criar índices para melhor performance
-- =====================================================
CREATE INDEX idx_permissoes_nome ON permissoes(nome);
CREATE INDEX idx_perfis_permissoes_permissao ON perfis_permissoes(permissao_id);

-- =====================================================
-- Comentários e Observações
-- =====================================================
-- Esta migration implementa um sistema de permissões granulares
-- que permite controle fino sobre quem pode fazer o quê no sistema.
--
-- Próximos passos:
-- 1. Atualizar @PreAuthorize nos controllers para usar hasAuthority()
-- 2. Implementar serviço de verificação de permissões
-- 3. Criar endpoints para gerenciamento de permissões via admin
