package br.com.casadoamor.sgca.modules.agendamento.repository;

import br.com.casadoamor.sgca.modules.agendamento.entity.HorarioProfissional;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioProfissionalRepository extends JpaRepository<HorarioProfissional, Long> {

    List<HorarioProfissional> findByProfissionalUsuario(AuthUsuario profissionalUsuario);

    @Query("SELECT h FROM HorarioProfissional h WHERE h.profissionalUsuario = :profissional " +
           "AND h.diaSemana = :diaSemana AND h.ativo = true")
    List<HorarioProfissional> findByProfissionalAndDia(
        @Param("profissional") AuthUsuario profissional,
        @Param("diaSemana") Integer diaSemana
    );

    @Query("SELECT h FROM HorarioProfissional h WHERE h.profissionalUsuario = :profissional " +
           "AND h.ativo = true ORDER BY h.diaSemana, h.horaInicio")
    List<HorarioProfissional> findHorariosAtivos(@Param("profissional") AuthUsuario profissional);

    @Query("SELECT h FROM HorarioProfissional h WHERE h.profissionalUsuario = :profissional " +
           "AND h.diaSemana = :diaSemana AND h.ativo = true " +
           "AND h.horaInicio <= :hora AND h.horaFim >= :hora")
    Optional<HorarioProfissional> findHorarioDisponivel(
        @Param("profissional") AuthUsuario profissional,
        @Param("diaSemana") Integer diaSemana,
        @Param("hora") java.time.LocalTime hora
    );

    boolean existsByProfissionalUsuarioAndDiaSemanaAndAtivoTrue(
        AuthUsuario profissionalUsuario,
        Integer diaSemana
    );
}
