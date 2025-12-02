-- Migration para criar triggers de agendamento automático de triagens
-- Quando uma hospedagem é criada, automaticamente agenda ENF_TRIAGEM
-- Se o paciente usa sonda, também agenda NUT_TRIAGEM

DELIMITER $$

-- =====================================================
-- TRIGGER: Agendar triagem de enfermagem automaticamente
-- =====================================================
CREATE TRIGGER trg_agendar_triagem_enfermagem_after_hospedagem
AFTER INSERT ON hospedagens
FOR EACH ROW
BEGIN
    DECLARE v_tipo_servico_enf_id BIGINT;
    DECLARE v_duracao_enf INT;
    DECLARE v_data_hora_inicio DATETIME;
    DECLARE v_data_hora_fim DATETIME;
    
    -- Buscar o ID e duração do serviço ENF_TRIAGEM
    SELECT id, duracao_minutos 
    INTO v_tipo_servico_enf_id, v_duracao_enf
    FROM tipos_servico 
    WHERE codigo = 'ENF_TRIAGEM' AND ativo = TRUE
    LIMIT 1;
    
    -- Se encontrou o tipo de serviço
    IF v_tipo_servico_enf_id IS NOT NULL THEN
        -- Define o horário da triagem para o mesmo dia da entrada, às 08:00
        SET v_data_hora_inicio = CONCAT(NEW.data_entrada, ' 08:00:00');
        SET v_data_hora_fim = DATE_ADD(v_data_hora_inicio, INTERVAL v_duracao_enf MINUTE);
        
        -- Inserir agendamento automático
        INSERT INTO agendamentos_pacientes (
            uuid,
            paciente_id,
            tipo_servico_id,
            hospedagem_id,
            data_hora_inicio,
            data_hora_fim,
            duracao_minutos,
            status,
            tipo_atendimento,
            modalidade,
            prioridade,
            gerado_automaticamente,
            motivo_geracao_automatica,
            created_at,
            created_by
        ) VALUES (
            UUID(),
            NEW.paciente_id,
            v_tipo_servico_enf_id,
            NEW.id,
            v_data_hora_inicio,
            v_data_hora_fim,
            v_duracao_enf,
            'AGENDADO',
            'TRIAGEM',
            'PRESENCIAL',
            'ALTA',
            TRUE,
            'Triagem automática na entrada da hospedagem',
            NOW(),
            NEW.created_by
        );
    END IF;
END$$

DELIMITER ;

-- Comentário sobre trigger de nutrição:
-- A triagem de nutrição (NUT_TRIAGEM) será agendada por lógica de aplicação
-- quando for identificado uso de sonda pelo paciente, pois essa informação
-- pode estar em campos específicos do paciente ou ser registrada posteriormente
