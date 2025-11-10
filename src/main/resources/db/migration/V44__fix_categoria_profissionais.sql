-- Corrige enum da coluna categoria para representar áreas de atuação profissional
-- Remove valores antigos (FUNCIONARIO, VOLUNTARIO) que agora estão em tipo_vinculo

ALTER TABLE profissionais 
MODIFY COLUMN categoria ENUM(
    'MEDICO',
    'ENFERMAGEM',
    'ODONTOLOGIA',
    'PSICOLOGIA',
    'NUTRICAO',
    'FISIOTERAPIA',
    'ASSISTENCIA_SOCIAL',
    'PEDAGOGIA',
    'ADMINISTRATIVO',
    'RECEPCAO',
    'OUTROS'
) NOT NULL COMMENT 'Área de atuação do profissional';
