-- Migration para popular dados iniciais do sistema de agendamentos

-- Nota: Os profissionais de saúde devem ser cadastrados manualmente através da aplicação
-- Aqui vamos apenas garantir que os tipos de serviço necessários existam

-- Verificar se ENF_TRIAGEM existe (já deve existir da V63)
-- Verificar se NUT_TRIAGEM existe (já deve existir da V63)

-- Adicionar mais tipos de serviço se necessário
INSERT IGNORE INTO tipos_servico (codigo, nome, descricao, categoria, duracao_minutos, requer_profissional, permite_acompanhante, ativo, created_at) VALUES
-- Médico
('MED_TRIAGEM', 'Triagem Médica', 'Avaliação médica inicial', 'MEDICO', 20, 1, 1, 1, NOW()),
('MED_EMERGENCIA', 'Atendimento de Emergência', 'Atendimento médico emergencial', 'MEDICO', 30, 1, 1, 1, NOW()),

-- Odontológico
('ODONTO_TRIAGEM', 'Triagem Odontológica', 'Avaliação odontológica inicial', 'ODONTOLOGICO', 15, 1, 1, 1, NOW()),
('ODONTO_EMERGENCIA', 'Emergência Odontológica', 'Atendimento odontológico emergencial', 'ODONTOLOGICO', 30, 1, 1, 1, NOW()),

-- Fisioterapia
('FISIO_AVALIACAO_INICIAL', 'Avaliação Fisioterapêutica Inicial', 'Primeira avaliação fisioterapêutica', 'FISIOTERAPIA', 50, 1, 1, 1, NOW()),

-- Psicologia
('PSI_TRIAGEM', 'Triagem Psicológica', 'Avaliação psicológica inicial', 'PSICOLOGIA', 30, 1, 1, 1, NOW()),
('PSI_AVALIACAO_INICIAL', 'Avaliação Psicológica Inicial', 'Primeira avaliação psicológica completa', 'PSICOLOGIA', 60, 1, 1, 1, NOW()),

-- Assistência Social
('AS_TRIAGEM', 'Triagem Social', 'Avaliação social inicial', 'ASSISTENCIA_SOCIAL', 30, 1, 1, 1, NOW()),
('AS_AVALIACAO_INICIAL', 'Avaliação Social Inicial', 'Primeira avaliação social completa', 'ASSISTENCIA_SOCIAL', 45, 1, 1, 1, NOW());

-- Comentário sobre profissionais:
-- Os profissionais devem ser vinculados através da tabela profissionais_especialidades
-- após serem criados como usuários no auth_usuarios
-- Exemplo de como vincular um profissional:
-- 
-- INSERT INTO profissionais_especialidades (
--     profissional_usuario_id, 
--     categoria_servico, 
--     registro_profissional, 
--     registro_uf, 
--     especialidade,
--     ativo,
--     disponivel_agendamento,
--     duracao_padrao_consulta
-- ) VALUES (
--     1, -- ID do usuário em auth_usuarios
--     'MEDICO',
--     'CRM 12345',
--     'SP',
--     'Clínico Geral',
--     1,
--     1,
--     30
-- );
