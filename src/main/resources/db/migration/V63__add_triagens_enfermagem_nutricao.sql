-- Migration para adicionar triagens de enfermagem e nutrição

INSERT INTO tipos_servico (
    codigo, 
    nome, 
    descricao, 
    categoria, 
    duracao_minutos, 
    requer_profissional, 
    permite_acompanhante, 
    ativo, 
    created_at
) VALUES
-- Triagem de Enfermagem
(
    'ENF_TRIAGEM', 
    'Triagem', 
    'Avaliação inicial da saúde do paciente', 
    'ENFERMAGEM', 
    15, 
    1, 
    1, 
    1, 
    NOW()
),
-- Triagem de Nutrição
(
    'NUT_TRIAGEM', 
    'Triagem', 
    'Avaliação nutricional inicial', 
    'NUTRICAO', 
    15, 
    1, 
    1, 
    1, 
    NOW()
);
