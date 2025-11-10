package br.com.casadoamor.sgca.modules.agendamento.repository;

import br.com.casadoamor.sgca.modules.agendamento.entity.AtividadeGrupo;
import br.com.casadoamor.sgca.modules.agendamento.entity.InscricaoAtividade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusInscricao;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento de Inscrições em Atividades
 */
@Repository
public interface InscricaoAtividadeRepository extends JpaRepository<InscricaoAtividade, Long> {

    /**
     * Busca inscrição por atividade e paciente
     */
    Optional<InscricaoAtividade> findByAtividadeAndPaciente(AtividadeGrupo atividade, Paciente paciente);

    /**
     * Lista todas as inscrições de uma atividade
     */
    List<InscricaoAtividade> findByAtividade(AtividadeGrupo atividade);

    /**
     * Lista inscrições de uma atividade por status
     */
    List<InscricaoAtividade> findByAtividadeAndStatus(AtividadeGrupo atividade, StatusInscricao status);

    /**
     * Lista inscrições confirmadas de uma atividade
     */
    List<InscricaoAtividade> findByAtividadeAndStatusOrderByInscritoEmAsc(AtividadeGrupo atividade, StatusInscricao status);

    /**
     * Lista todas as inscrições de um paciente
     */
    List<InscricaoAtividade> findByPaciente(Paciente paciente);

    /**
     * Lista inscrições de um paciente por status
     */
    List<InscricaoAtividade> findByPacienteAndStatus(Paciente paciente, StatusInscricao status);

    /**
     * Lista inscrições pendentes de um paciente
     */
    @Query("SELECT i FROM InscricaoAtividade i WHERE i.paciente = :paciente " +
           "AND i.status IN ('PENDENTE', 'CONFIRMADA') AND i.atividade.dataInicio > :agora")
    List<InscricaoAtividade> findInscricoesAtivasPaciente(@Param("paciente") Paciente paciente, @Param("agora") LocalDateTime agora);

    /**
     * Conta inscrições de uma atividade por status
     */
    long countByAtividadeAndStatus(AtividadeGrupo atividade, StatusInscricao status);

    /**
     * Conta total de inscrições de uma atividade
     */
    long countByAtividade(AtividadeGrupo atividade);

    /**
     * Conta total de inscrições de um paciente
     */
    long countByPaciente(Paciente paciente);

    /**
     * Verifica se paciente já está inscrito na atividade
     */
    boolean existsByAtividadeAndPaciente(AtividadeGrupo atividade, Paciente paciente);

    /**
     * Verifica se paciente tem inscrição confirmada na atividade
     */
    @Query("SELECT COUNT(i) > 0 FROM InscricaoAtividade i WHERE i.atividade = :atividade " +
           "AND i.paciente = :paciente AND i.status = 'CONFIRMADA'")
    boolean hasInscricaoConfirmada(@Param("atividade") AtividadeGrupo atividade, @Param("paciente") Paciente paciente);

    /**
     * Lista pacientes em lista de espera ordenados por data de inscrição
     */
    @Query("SELECT i FROM InscricaoAtividade i WHERE i.atividade = :atividade " +
           "AND i.status = 'LISTA_ESPERA' ORDER BY i.inscritoEm ASC")
    List<InscricaoAtividade> findListaEsperaOrdenada(@Param("atividade") AtividadeGrupo atividade);

    /**
     * Busca primeira posição da lista de espera
     */
    @Query("SELECT i FROM InscricaoAtividade i WHERE i.atividade = :atividade " +
           "AND i.status = 'LISTA_ESPERA' ORDER BY i.inscritoEm ASC LIMIT 1")
    Optional<InscricaoAtividade> findPrimeiroListaEspera(@Param("atividade") AtividadeGrupo atividade);

    /**
     * Conta vagas ocupadas (inscrições confirmadas) em uma atividade
     */
    @Query("SELECT COUNT(i) FROM InscricaoAtividade i WHERE i.atividade = :atividade AND i.status = 'CONFIRMADA'")
    long countVagasOcupadas(@Param("atividade") AtividadeGrupo atividade);

    /**
     * Verifica se atividade está com vagas esgotadas
     */
    @Query("SELECT CASE WHEN COUNT(i) >= :capacidade THEN true ELSE false END " +
           "FROM InscricaoAtividade i WHERE i.atividade = :atividade AND i.status = 'CONFIRMADA'")
    boolean isVagasEsgotadas(@Param("atividade") AtividadeGrupo atividade, @Param("capacidade") Integer capacidade);
}
