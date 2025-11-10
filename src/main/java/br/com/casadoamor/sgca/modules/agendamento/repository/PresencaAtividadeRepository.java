package br.com.casadoamor.sgca.modules.agendamento.repository;

import br.com.casadoamor.sgca.modules.agendamento.entity.AtividadeGrupo;
import br.com.casadoamor.sgca.modules.agendamento.entity.PresencaAtividade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.NivelParticipacao;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento de Presenças em Atividades
 */
@Repository
public interface PresencaAtividadeRepository extends JpaRepository<PresencaAtividade, Long> {

    /**
     * Busca presença por atividade e paciente
     */
    Optional<PresencaAtividade> findByAtividadeAndPaciente(AtividadeGrupo atividade, Paciente paciente);

    /**
     * Lista todas as presenças de uma atividade
     */
    List<PresencaAtividade> findByAtividade(AtividadeGrupo atividade);

    /**
     * Lista presenças confirmadas de uma atividade
     */
    List<PresencaAtividade> findByAtividadeAndPresenteTrue(AtividadeGrupo atividade);

    /**
     * Lista ausências de uma atividade
     */
    List<PresencaAtividade> findByAtividadeAndPresenteFalse(AtividadeGrupo atividade);

    /**
     * Lista presenças de um paciente
     */
    List<PresencaAtividade> findByPaciente(Paciente paciente);

    /**
     * Lista presenças confirmadas de um paciente
     */
    List<PresencaAtividade> findByPacienteAndPresenteTrue(Paciente paciente);

    /**
     * Lista presenças por nível de participação
     */
    List<PresencaAtividade> findByNivelParticipacao(NivelParticipacao nivelParticipacao);

    /**
     * Lista presenças de uma atividade por nível de participação
     */
    List<PresencaAtividade> findByAtividadeAndNivelParticipacao(AtividadeGrupo atividade, NivelParticipacao nivelParticipacao);

    /**
     * Conta presenças confirmadas em uma atividade
     */
    long countByAtividadeAndPresenteTrue(AtividadeGrupo atividade);

    /**
     * Conta ausências em uma atividade
     */
    long countByAtividadeAndPresenteFalse(AtividadeGrupo atividade);

    /**
     * Conta total de presenças de um paciente
     */
    long countByPacienteAndPresenteTrue(Paciente paciente);

    /**
     * Verifica se paciente está registrado na atividade
     */
    boolean existsByAtividadeAndPaciente(AtividadeGrupo atividade, Paciente paciente);

    /**
     * Busca pacientes com alta participação em uma atividade
     */
    @Query("SELECT p FROM PresencaAtividade p WHERE p.atividade = :atividade " +
           "AND p.nivelParticipacao IN ('ALTA', 'EXCELENTE')")
    List<PresencaAtividade> findParticipantesAtivos(@Param("atividade") AtividadeGrupo atividade);

    /**
     * Calcula taxa de presença geral de uma atividade
     */
    @Query("SELECT (COUNT(p) * 100.0 / (SELECT COUNT(p2) FROM PresencaAtividade p2 WHERE p2.atividade = :atividade)) " +
           "FROM PresencaAtividade p WHERE p.atividade = :atividade AND p.presente = true")
    Double calcularTaxaPresenca(@Param("atividade") AtividadeGrupo atividade);
}
