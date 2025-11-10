package br.com.casadoamor.sgca.modules.agendamento.repository;

import br.com.casadoamor.sgca.modules.agendamento.entity.AtividadeGrupo;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.Modalidade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAtividade;
import br.com.casadoamor.sgca.modules.funcionario.entity.Profissional;
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
 * Repository para gerenciamento de Atividades em Grupo
 */
@Repository
public interface AtividadeGrupoRepository extends JpaRepository<AtividadeGrupo, Long> {

    /**
     * Busca atividade por UUID
     */
    Optional<AtividadeGrupo> findByUuid(String uuid);

    /**
     * Lista atividades por status
     */
    List<AtividadeGrupo> findByStatus(StatusAtividade status);

    /**
     * Lista atividades por modalidade
     */
    List<AtividadeGrupo> findByModalidade(Modalidade modalidade);

    /**
     * Lista atividades por responsável principal
     */
    List<AtividadeGrupo> findByResponsavelPrincipal(Profissional responsavel);

    /**
     * Lista atividades por responsável de apoio
     */
    List<AtividadeGrupo> findByResponsavelApoio(Profissional responsavel);

    /**
     * Busca atividades onde o profissional é responsável (principal ou apoio)
     */
    @Query("SELECT a FROM AtividadeGrupo a WHERE a.responsavelPrincipal = :profissional OR a.responsavelApoio = :profissional")
    List<AtividadeGrupo> findByResponsavel(@Param("profissional") Profissional profissional);

    /**
     * Busca atividades em um período
     */
    @Query("SELECT a FROM AtividadeGrupo a WHERE a.dataInicio BETWEEN :dataInicio AND :dataFim ORDER BY a.dataInicio")
    List<AtividadeGrupo> findByDataInicioBetween(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca atividades futuras por status
     */
    @Query("SELECT a FROM AtividadeGrupo a WHERE a.dataInicio > :agora AND a.status = :status ORDER BY a.dataInicio")
    List<AtividadeGrupo> findAtividadesFuturasPorStatus(@Param("agora") LocalDateTime agora, @Param("status") StatusAtividade status);

    /**
     * Busca próximas atividades (planejadas ou confirmadas)
     */
    @Query("SELECT a FROM AtividadeGrupo a WHERE a.dataInicio > :agora " +
           "AND a.status IN ('PLANEJADA', 'CONFIRMADA') ORDER BY a.dataInicio")
    List<AtividadeGrupo> findProximasAtividades(@Param("agora") LocalDateTime agora);

    /**
     * Busca atividades com inscrições abertas
     */
    @Query("SELECT a FROM AtividadeGrupo a WHERE a.inscricoesAbertas = true " +
           "AND a.dataInicio > :agora AND a.status IN ('PLANEJADA', 'CONFIRMADA') ORDER BY a.dataInicio")
    List<AtividadeGrupo> findAtividadesComInscricoesAbertas(@Param("agora") LocalDateTime agora);

    /**
     * Busca atividades por tipo
     */
    @Query("SELECT a FROM AtividadeGrupo a WHERE LOWER(a.tipo) LIKE LOWER(CONCAT('%', :tipo, '%'))")
    List<AtividadeGrupo> searchByTipo(@Param("tipo") String tipo);

    /**
     * Busca atividades por título
     */
    @Query("SELECT a FROM AtividadeGrupo a WHERE LOWER(a.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<AtividadeGrupo> searchByTitulo(@Param("titulo") String titulo);

    /**
     * Busca atividades por público-alvo
     */
    @Query("SELECT a FROM AtividadeGrupo a WHERE LOWER(a.publicoAlvo) LIKE LOWER(CONCAT('%', :publicoAlvo, '%'))")
    List<AtividadeGrupo> searchByPublicoAlvo(@Param("publicoAlvo") String publicoAlvo);

    /**
     * Busca atividades compatíveis com idade
     */
    @Query("SELECT a FROM AtividadeGrupo a WHERE " +
           "(a.idadeMinima IS NULL OR a.idadeMinima <= :idade) AND " +
           "(a.idadeMaxima IS NULL OR a.idadeMaxima >= :idade) AND " +
           "a.status IN ('PLANEJADA', 'CONFIRMADA') AND a.dataInicio > :agora")
    List<AtividadeGrupo> findAtividadesParaIdade(@Param("idade") Integer idade, @Param("agora") LocalDateTime agora);

    /**
     * Busca atividades do dia para um responsável
     */
    @Query("SELECT a FROM AtividadeGrupo a WHERE " +
           "(a.responsavelPrincipal = :profissional OR a.responsavelApoio = :profissional) " +
           "AND DATE(a.dataInicio) = DATE(:data) ORDER BY a.dataInicio")
    List<AtividadeGrupo> findAtividadesDoDia(@Param("profissional") Profissional profissional, @Param("data") LocalDateTime data);

    /**
     * Verifica conflito de horário para um profissional
     */
    @Query("SELECT COUNT(a) > 0 FROM AtividadeGrupo a WHERE " +
           "(a.responsavelPrincipal = :profissional OR a.responsavelApoio = :profissional) " +
           "AND a.status NOT IN ('CANCELADA', 'ADIADA') " +
           "AND ((a.dataInicio <= :dataInicio AND a.dataFim > :dataInicio) " +
           "OR (a.dataInicio < :dataFim AND a.dataFim >= :dataFim) " +
           "OR (a.dataInicio >= :dataInicio AND a.dataFim <= :dataFim))")
    boolean hasConflitoHorario(@Param("profissional") Profissional profissional,
                               @Param("dataInicio") LocalDateTime dataInicio,
                               @Param("dataFim") LocalDateTime dataFim);

    /**
     * Lista atividades com paginação
     */
    Page<AtividadeGrupo> findByStatus(StatusAtividade status, Pageable pageable);

    /**
     * Conta atividades por status
     */
    long countByStatus(StatusAtividade status);

    /**
     * Conta atividades de um responsável principal
     */
    long countByResponsavelPrincipal(Profissional responsavel);
}
