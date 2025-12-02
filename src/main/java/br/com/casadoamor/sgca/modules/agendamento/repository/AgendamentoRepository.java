package br.com.casadoamor.sgca.modules.agendamento.repository;

import br.com.casadoamor.sgca.modules.agendamento.entity.Agendamento;
import br.com.casadoamor.sgca.modules.agendamento.entity.TipoServico;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Modalidade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.funcionario.entity.Profissional;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento de Agendamentos
 */
@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /**
     * Busca agendamento por UUID
     */
    Optional<Agendamento> findByUuid(String uuid);

    /**
     * Lista agendamentos de um paciente
     */
    List<Agendamento> findByPaciente(Paciente paciente);

    /**
     * Lista agendamentos de um paciente por status
     */
    List<Agendamento> findByPacienteAndStatus(Paciente paciente, StatusAgendamento status);

    /**
     * Lista agendamentos de um profissional
     */
    List<Agendamento> findByProfissional(Profissional profissional);

    /**
     * Lista agendamentos de um profissional por status
     */
    List<Agendamento> findByProfissionalAndStatus(Profissional profissional, StatusAgendamento status);

    /**
     * Lista agendamentos por tipo de serviço
     */
    List<Agendamento> findByTipoServico(TipoServico tipoServico);

    /**
     * Lista agendamentos por status
     */
    List<Agendamento> findByStatus(StatusAgendamento status);

    /**
     * Lista agendamentos por modalidade
     */
    List<Agendamento> findByModalidade(Modalidade modalidade);

    /**
     * Busca agendamentos em um período
     */
    @Query("SELECT a FROM Agendamento a WHERE a.dataInicio BETWEEN :dataInicio AND :dataFim ORDER BY a.dataInicio")
    List<Agendamento> findByDataInicioBetween(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca agendamentos de um paciente em um período
     */
    @Query("SELECT a FROM Agendamento a WHERE a.paciente = :paciente AND a.dataInicio BETWEEN :dataInicio AND :dataFim ORDER BY a.dataInicio")
    List<Agendamento> findByPacienteAndDataInicioBetween(@Param("paciente") Paciente paciente, @Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca agendamentos de um profissional em um período
     */
    @Query("SELECT a FROM Agendamento a WHERE a.profissional = :profissional AND a.dataInicio BETWEEN :dataInicio AND :dataFim ORDER BY a.dataInicio")
    List<Agendamento> findByProfissionalAndDataInicioBetween(@Param("profissional") Profissional profissional, @Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca agendamentos pendentes de lembrete
     */
    @Query("SELECT a FROM Agendamento a WHERE a.lembreteEnviado = false AND a.status IN ('AGENDADO', 'CONFIRMADO') AND a.dataInicio > :agora")
    List<Agendamento> findAgendamentosPendentesLembrete(@Param("agora") LocalDateTime agora);

    /**
     * Busca agendamentos que precisam de confirmação
     */
    @Query("SELECT a FROM Agendamento a WHERE a.confirmacaoPaciente = false AND a.status = 'AGENDADO' AND a.dataInicio > :agora")
    List<Agendamento> findAgendamentosPendentesConfirmacao(@Param("agora") LocalDateTime agora);

    /**
     * Verifica conflito de horário para um profissional
     */
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a WHERE a.profissional = :profissional " +
           "AND a.status NOT IN ('CANCELADO', 'REMARCADO') " +
           "AND ((a.dataInicio <= :dataInicio AND a.dataFim > :dataInicio) " +
           "OR (a.dataInicio < :dataFim AND a.dataFim >= :dataFim) " +
           "OR (a.dataInicio >= :dataInicio AND a.dataFim <= :dataFim))")
    boolean hasConflitoHorario(@Param("profissional") Profissional profissional,
                               @Param("dataInicio") LocalDateTime dataInicio,
                               @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca próximos agendamentos de um paciente
     */
    @Query("SELECT a FROM Agendamento a WHERE a.paciente = :paciente AND a.dataInicio > :agora " +
           "AND a.status IN ('AGENDADO', 'CONFIRMADO') ORDER BY a.dataInicio")
    List<Agendamento> findProximosAgendamentosPaciente(@Param("paciente") Paciente paciente, @Param("agora") LocalDateTime agora);

    /**
     * Busca próximos agendamentos de um profissional
     */
    @Query("SELECT a FROM Agendamento a WHERE a.profissional = :profissional AND a.dataInicio > :agora " +
           "AND a.status IN ('AGENDADO', 'CONFIRMADO') ORDER BY a.dataInicio")
    List<Agendamento> findProximosAgendamentosProfissional(@Param("profissional") Profissional profissional, @Param("agora") LocalDateTime agora);

    /**
     * Lista agendamentos com paginação
     */
    Page<Agendamento> findByStatus(StatusAgendamento status, Pageable pageable);

    /**
     * Busca agendamentos do dia para um profissional
     */
    @Query("SELECT a FROM Agendamento a WHERE a.profissional = :profissional " +
           "AND DATE(a.dataInicio) = DATE(:data) ORDER BY a.dataInicio")
    List<Agendamento> findAgendamentosDoDia(@Param("profissional") Profissional profissional, @Param("data") LocalDateTime data);

    /**
     * Conta agendamentos por status
     */
    long countByStatus(StatusAgendamento status);

    /**
     * Conta agendamentos de um paciente
     */
    long countByPaciente(Paciente paciente);

    /**
     * Conta agendamentos de um profissional
     */
    long countByProfissional(Profissional profissional);

    // === ESTATÍSTICAS PARA DASHBOARD ===
    
    /**
     * Conta agendamentos do dia
     */
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE DATE(a.dataInicio) = CURRENT_DATE")
    Long contarAgendamentosHoje();
    
    /**
     * Conta agendamentos da semana
     */
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE YEARWEEK(a.dataInicio, 1) = YEARWEEK(CURRENT_DATE, 1)")
    Long contarAgendamentosSemana();
    
    /**
     * Conta agendamentos pendentes (AGENDADO, mas sem confirmação)
     */
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.status = 'AGENDADO' " +
           "AND a.confirmacaoPaciente = false AND a.dataInicio > CURRENT_TIMESTAMP")
    Long contarAgendamentosPendentes();
}
