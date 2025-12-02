package br.com.casadoamor.sgca.modules.agendamento.repository;

import br.com.casadoamor.sgca.modules.agendamento.entity.AgendamentoAcompanhante;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoAcompanhanteRepository extends JpaRepository<AgendamentoAcompanhante, Long>, JpaSpecificationExecutor<AgendamentoAcompanhante> {

    Optional<AgendamentoAcompanhante> findByUuid(String uuid);

    List<AgendamentoAcompanhante> findByAcompanhante(Acompanhante acompanhante);

    List<AgendamentoAcompanhante> findByProfissionalUsuario(AuthUsuario profissionalUsuario);

    List<AgendamentoAcompanhante> findByPacienteVinculado(Paciente paciente);

    List<AgendamentoAcompanhante> findByStatus(StatusAgendamento status);

    @Query("SELECT a FROM AgendamentoAcompanhante a WHERE a.profissionalUsuario = :profissional " +
           "AND a.dataHoraInicio BETWEEN :inicio AND :fim " +
           "AND a.status NOT IN ('CANCELADO', 'REMARCADO') " +
           "AND a.deletedAt IS NULL")
    List<AgendamentoAcompanhante> findByProfissionalAndPeriodo(
        @Param("profissional") AuthUsuario profissional,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT a FROM AgendamentoAcompanhante a WHERE a.acompanhante = :acompanhante " +
           "AND a.dataHoraInicio BETWEEN :inicio AND :fim " +
           "AND a.deletedAt IS NULL")
    List<AgendamentoAcompanhante> findByAcompanhanteAndPeriodo(
        @Param("acompanhante") Acompanhante acompanhante,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT COUNT(a) FROM AgendamentoAcompanhante a WHERE a.dataHoraInicio >= :inicio " +
           "AND a.dataHoraInicio < :fim AND a.deletedAt IS NULL")
    Long countByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(a) FROM AgendamentoAcompanhante a WHERE a.status = :status AND a.deletedAt IS NULL")
    Long countByStatus(@Param("status") StatusAgendamento status);
}
