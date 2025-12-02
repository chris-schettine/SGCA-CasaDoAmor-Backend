package br.com.casadoamor.sgca.modules.agendamento.repository;

import br.com.casadoamor.sgca.modules.agendamento.entity.AgendamentoPaciente;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.StatusAgendamento;
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
public interface AgendamentoPacienteRepository extends JpaRepository<AgendamentoPaciente, Long>, JpaSpecificationExecutor<AgendamentoPaciente> {

    Optional<AgendamentoPaciente> findByUuid(String uuid);

    List<AgendamentoPaciente> findByPaciente(Paciente paciente);

    List<AgendamentoPaciente> findByProfissionalUsuario(AuthUsuario profissionalUsuario);

    List<AgendamentoPaciente> findByStatus(StatusAgendamento status);

    @Query("SELECT a FROM AgendamentoPaciente a WHERE a.profissionalUsuario = :profissional " +
           "AND a.dataHoraInicio BETWEEN :inicio AND :fim " +
           "AND a.status NOT IN ('CANCELADO', 'REMARCADO') " +
           "AND a.deletedAt IS NULL")
    List<AgendamentoPaciente> findByProfissionalAndPeriodo(
        @Param("profissional") AuthUsuario profissional,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT a FROM AgendamentoPaciente a WHERE a.paciente = :paciente " +
           "AND a.dataHoraInicio BETWEEN :inicio AND :fim " +
           "AND a.deletedAt IS NULL")
    List<AgendamentoPaciente> findByPacienteAndPeriodo(
        @Param("paciente") Paciente paciente,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT COUNT(a) FROM AgendamentoPaciente a WHERE a.dataHoraInicio >= :inicio " +
           "AND a.dataHoraInicio < :fim AND a.deletedAt IS NULL")
    Long countByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(a) FROM AgendamentoPaciente a WHERE a.status = :status AND a.deletedAt IS NULL")
    Long countByStatus(@Param("status") StatusAgendamento status);

    @Query("SELECT a FROM AgendamentoPaciente a WHERE a.geradoAutomaticamente = true " +
           "AND a.dataHoraInicio BETWEEN :inicio AND :fim AND a.deletedAt IS NULL")
    List<AgendamentoPaciente> findAgendamentosAutomaticos(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
}
