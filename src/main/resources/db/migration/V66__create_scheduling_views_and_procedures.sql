-- Migration para criar views e procedures para estatísticas de agendamentos

-- =====================================================
-- VIEW: Visão consolidada de agendamentos (pacientes e acompanhantes)
-- =====================================================
CREATE OR REPLACE VIEW v_agendamentos_consolidados AS
SELECT 
    'PACIENTE' as tipo_pessoa,
    ap.id,
    ap.uuid,
    ap.paciente_id as pessoa_id,
    dp.nome as pessoa_nome,
    ap.tipo_servico_id,
    ts.codigo as servico_codigo,
    ts.nome as servico_nome,
    ts.categoria as servico_categoria,
    ap.profissional_usuario_id,
    au.nome as profissional_nome,
    ap.data_hora_inicio,
    ap.data_hora_fim,
    ap.duracao_minutos,
    ap.status,
    ap.tipo_atendimento,
    ap.modalidade,
    ap.prioridade,
    ap.gerado_automaticamente,
    ap.confirmado_paciente as confirmado,
    ap.compareceu,
    ap.created_at,
    ap.deleted_at
FROM agendamentos_pacientes ap
INNER JOIN pacientes p ON ap.paciente_id = p.id
INNER JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id
INNER JOIN tipos_servico ts ON ap.tipo_servico_id = ts.id
LEFT JOIN auth_usuarios au ON ap.profissional_usuario_id = au.id
WHERE ap.deleted_at IS NULL

UNION ALL

SELECT 
    'ACOMPANHANTE' as tipo_pessoa,
    aa.id,
    aa.uuid,
    aa.acompanhante_id as pessoa_id,
    da.nome as pessoa_nome,
    aa.tipo_servico_id,
    ts.codigo as servico_codigo,
    ts.nome as servico_nome,
    ts.categoria as servico_categoria,
    aa.profissional_usuario_id,
    au.nome as profissional_nome,
    aa.data_hora_inicio,
    aa.data_hora_fim,
    aa.duracao_minutos,
    aa.status,
    aa.tipo_atendimento,
    aa.modalidade,
    aa.prioridade,
    FALSE as gerado_automaticamente,
    aa.confirmado_acompanhante as confirmado,
    aa.compareceu,
    aa.created_at,
    aa.deleted_at
FROM agendamentos_acompanhantes aa
INNER JOIN acompanhantes a ON aa.acompanhante_id = a.id
INNER JOIN dados_pessoais da ON a.dado_pessoal_id = da.id
INNER JOIN tipos_servico ts ON aa.tipo_servico_id = ts.id
LEFT JOIN auth_usuarios au ON aa.profissional_usuario_id = au.id
WHERE aa.deleted_at IS NULL;

-- =====================================================
-- STORED PROCEDURE: Estatísticas de agendamentos
-- =====================================================
DELIMITER $$

CREATE PROCEDURE sp_estatisticas_agendamentos()
BEGIN
    -- Estatísticas Gerais
    SELECT 
        'GERAL' as categoria,
        COUNT(*) as total_agendamentos,
        SUM(CASE WHEN status = 'AGENDADO' THEN 1 ELSE 0 END) as agendados,
        SUM(CASE WHEN status = 'CONFIRMADO' THEN 1 ELSE 0 END) as confirmados,
        SUM(CASE WHEN status = 'EM_ATENDIMENTO' THEN 1 ELSE 0 END) as em_atendimento,
        SUM(CASE WHEN status = 'CONCLUIDO' THEN 1 ELSE 0 END) as concluidos,
        SUM(CASE WHEN status = 'CANCELADO' THEN 1 ELSE 0 END) as cancelados,
        SUM(CASE WHEN status = 'NAO_COMPARECEU' THEN 1 ELSE 0 END) as nao_compareceram,
        SUM(CASE WHEN DATE(data_hora_inicio) = CURDATE() THEN 1 ELSE 0 END) as agendamentos_hoje,
        SUM(CASE WHEN DATE(data_hora_inicio) = CURDATE() + INTERVAL 1 DAY THEN 1 ELSE 0 END) as agendamentos_amanha,
        SUM(CASE WHEN WEEK(data_hora_inicio) = WEEK(CURDATE()) AND YEAR(data_hora_inicio) = YEAR(CURDATE()) THEN 1 ELSE 0 END) as agendamentos_semana,
        SUM(CASE WHEN MONTH(data_hora_inicio) = MONTH(CURDATE()) AND YEAR(data_hora_inicio) = YEAR(CURDATE()) THEN 1 ELSE 0 END) as agendamentos_mes
    FROM v_agendamentos_consolidados
    WHERE deleted_at IS NULL;
    
    -- Estatísticas por Categoria de Serviço
    SELECT 
        'POR_CATEGORIA' as tipo_estatistica,
        servico_categoria,
        COUNT(*) as total,
        SUM(CASE WHEN status IN ('AGENDADO', 'CONFIRMADO') THEN 1 ELSE 0 END) as pendentes,
        SUM(CASE WHEN status = 'CONCLUIDO' THEN 1 ELSE 0 END) as concluidos,
        SUM(CASE WHEN status = 'CANCELADO' THEN 1 ELSE 0 END) as cancelados,
        ROUND(AVG(duracao_minutos), 2) as duracao_media_minutos
    FROM v_agendamentos_consolidados
    WHERE deleted_at IS NULL
    GROUP BY servico_categoria
    ORDER BY total DESC;
    
    -- Estatísticas por Profissional (Top 10)
    SELECT 
        'TOP_PROFISSIONAIS' as tipo_estatistica,
        profissional_usuario_id,
        profissional_nome,
        COUNT(*) as total_atendimentos,
        SUM(CASE WHEN status = 'CONCLUIDO' THEN 1 ELSE 0 END) as concluidos,
        SUM(CASE WHEN status = 'NAO_COMPARECEU' THEN 1 ELSE 0 END) as faltas,
        ROUND(SUM(CASE WHEN status = 'CONCLUIDO' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as taxa_conclusao_percentual
    FROM v_agendamentos_consolidados
    WHERE deleted_at IS NULL 
        AND profissional_usuario_id IS NOT NULL
    GROUP BY profissional_usuario_id, profissional_nome
    ORDER BY total_atendimentos DESC
    LIMIT 10;
    
    -- Taxa de Comparecimento
    SELECT 
        'TAXA_COMPARECIMENTO' as tipo_estatistica,
        COUNT(*) as total_agendamentos_realizados,
        SUM(CASE WHEN compareceu = TRUE THEN 1 ELSE 0 END) as comparecimentos,
        SUM(CASE WHEN compareceu = FALSE THEN 1 ELSE 0 END) as faltas,
        ROUND(SUM(CASE WHEN compareceu = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as taxa_comparecimento_percentual
    FROM v_agendamentos_consolidados
    WHERE deleted_at IS NULL 
        AND compareceu IS NOT NULL;
    
    -- Agendamentos por Tipo de Pessoa
    SELECT 
        'POR_TIPO_PESSOA' as tipo_estatistica,
        tipo_pessoa,
        COUNT(*) as total,
        SUM(CASE WHEN status IN ('AGENDADO', 'CONFIRMADO') THEN 1 ELSE 0 END) as pendentes,
        SUM(CASE WHEN status = 'CONCLUIDO' THEN 1 ELSE 0 END) as concluidos
    FROM v_agendamentos_consolidados
    WHERE deleted_at IS NULL
    GROUP BY tipo_pessoa;
    
    -- Serviços Mais Agendados (Top 15)
    SELECT 
        'SERVICOS_MAIS_AGENDADOS' as tipo_estatistica,
        servico_codigo,
        servico_nome,
        servico_categoria,
        COUNT(*) as total_agendamentos,
        SUM(CASE WHEN DATE(data_hora_inicio) >= CURDATE() THEN 1 ELSE 0 END) as agendamentos_futuros
    FROM v_agendamentos_consolidados
    WHERE deleted_at IS NULL
    GROUP BY servico_codigo, servico_nome, servico_categoria
    ORDER BY total_agendamentos DESC
    LIMIT 15;
    
    -- Horários mais ocupados (por hora do dia)
    SELECT 
        'HORARIOS_OCUPADOS' as tipo_estatistica,
        HOUR(data_hora_inicio) as hora,
        COUNT(*) as total_agendamentos,
        ROUND(AVG(duracao_minutos), 2) as duracao_media
    FROM v_agendamentos_consolidados
    WHERE deleted_at IS NULL
    GROUP BY HOUR(data_hora_inicio)
    ORDER BY hora;
    
    -- Triagens Automáticas vs Manuais
    SELECT 
        'TRIAGENS' as tipo_estatistica,
        SUM(CASE WHEN gerado_automaticamente = TRUE THEN 1 ELSE 0 END) as triagens_automaticas,
        SUM(CASE WHEN gerado_automaticamente = FALSE OR gerado_automaticamente IS NULL THEN 1 ELSE 0 END) as agendamentos_manuais,
        COUNT(*) as total
    FROM v_agendamentos_consolidados
    WHERE deleted_at IS NULL;
    
END$$

DELIMITER ;

-- =====================================================
-- STORED PROCEDURE: Agenda do profissional por dia
-- =====================================================
DELIMITER $$

CREATE PROCEDURE sp_agenda_profissional_dia(
    IN p_profissional_id BIGINT,
    IN p_data DATE
)
BEGIN
    SELECT 
        tipo_pessoa,
        uuid,
        pessoa_nome,
        servico_nome,
        servico_categoria,
        TIME(data_hora_inicio) as hora_inicio,
        TIME(data_hora_fim) as hora_fim,
        duracao_minutos,
        status,
        tipo_atendimento,
        modalidade,
        prioridade,
        confirmado,
        compareceu
    FROM v_agendamentos_consolidados
    WHERE profissional_usuario_id = p_profissional_id
        AND DATE(data_hora_inicio) = p_data
        AND deleted_at IS NULL
    ORDER BY data_hora_inicio;
END$$

DELIMITER ;

-- =====================================================
-- STORED PROCEDURE: Verificar conflito de horário
-- =====================================================
DELIMITER $$

CREATE PROCEDURE sp_verificar_conflito_horario(
    IN p_profissional_id BIGINT,
    IN p_data_hora_inicio DATETIME,
    IN p_data_hora_fim DATETIME,
    IN p_agendamento_id_excluir BIGINT
)
BEGIN
    -- Verifica conflitos em agendamentos de pacientes
    SELECT 
        'PACIENTE' as tipo,
        ap.uuid,
        dp.nome as pessoa_nome,
        ts.nome as servico,
        ap.data_hora_inicio,
        ap.data_hora_fim,
        ap.status
    FROM agendamentos_pacientes ap
    INNER JOIN pacientes p ON ap.paciente_id = p.id
    INNER JOIN dados_pessoais dp ON p.dado_pessoal_id = dp.id
    INNER JOIN tipos_servico ts ON ap.tipo_servico_id = ts.id
    WHERE ap.profissional_usuario_id = p_profissional_id
        AND ap.status NOT IN ('CANCELADO', 'REMARCADO')
        AND ap.deleted_at IS NULL
        AND (ap.id != p_agendamento_id_excluir OR p_agendamento_id_excluir IS NULL)
        AND (
            (ap.data_hora_inicio < p_data_hora_fim AND ap.data_hora_fim > p_data_hora_inicio)
        )
    
    UNION ALL
    
    -- Verifica conflitos em agendamentos de acompanhantes
    SELECT 
        'ACOMPANHANTE' as tipo,
        aa.uuid,
        da.nome as pessoa_nome,
        ts.nome as servico,
        aa.data_hora_inicio,
        aa.data_hora_fim,
        aa.status
    FROM agendamentos_acompanhantes aa
    INNER JOIN acompanhantes a ON aa.acompanhante_id = a.id
    INNER JOIN dados_pessoais da ON a.dado_pessoal_id = da.id
    INNER JOIN tipos_servico ts ON aa.tipo_servico_id = ts.id
    WHERE aa.profissional_usuario_id = p_profissional_id
        AND aa.status NOT IN ('CANCELADO', 'REMARCADO')
        AND aa.deleted_at IS NULL
        AND (aa.id != p_agendamento_id_excluir OR p_agendamento_id_excluir IS NULL)
        AND (
            (aa.data_hora_inicio < p_data_hora_fim AND aa.data_hora_fim > p_data_hora_inicio)
        )
    
    UNION ALL
    
    -- Verifica bloqueios de agenda
    SELECT 
        'BLOQUEIO' as tipo,
        CAST(ba.id AS CHAR) as uuid,
        ba.tipo as pessoa_nome,
        ba.motivo as servico,
        ba.data_hora_inicio,
        ba.data_hora_fim,
        'BLOQUEADO' as status
    FROM bloqueios_agenda ba
    WHERE ba.profissional_usuario_id = p_profissional_id
        AND (
            (ba.data_hora_inicio < p_data_hora_fim AND ba.data_hora_fim > p_data_hora_inicio)
        );
END$$

DELIMITER ;
