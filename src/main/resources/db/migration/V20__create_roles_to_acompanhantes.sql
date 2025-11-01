INSERT INTO permissoes (nome, descricao, criado_em) VALUES
('ACOMPANHANTES_CRIAR', 'Permissão para criar novos acompanhantes no sistema', NOW()),
('ACOMPANHANTES_EDITAR', 'Permissão para editar dados de acompanhantes existentes', NOW()),
('ACOMPANHANTES_VER', 'Permissão para visualizar acompanhantes do sistema', NOW()),
('ACOMPANHANTES_EXCLUIR', 'Permissão para excluir acompanhantes', NOW());

INSERT INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'ADMINISTRADOR';

INSERT INTO perfis_permissoes (perfil_id, permissao_id)
SELECT p.id, pm.id
FROM perfis p
CROSS JOIN permissoes pm
WHERE p.nome = 'RECEPCIONISTA'
  AND pm.nome IN (
    'ACOMPANHANTES_CRIAR', 'ACOMPANHANTES_EDITAR', 'ACOMPANHANTES_VER'
  );
