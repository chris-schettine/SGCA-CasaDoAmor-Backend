package br.com.casadoamor.sgca.modules.agendamento.repository;

import br.com.casadoamor.sgca.modules.agendamento.entity.BloqueioAgenda;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, Long> {

    List<BloqueioAgenda> findByProfissionalUsuario(AuthUsuario profissionalUsuario);

    @Query("SELECT b FROM BloqueioAgenda b WHERE b.profissionalUsuario = :profissional " +
           "AND b.ativo = true " +
           "AND ((b.dataHoraInicio <= :fim AND b.dataHoraFim >= :inicio))")
    List<BloqueioAgenda> findBloqueiosAtivosNoPeriodo(
        @Param("profissional") AuthUsuario profissional,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT b FROM BloqueioAgenda b WHERE b.profissionalUsuario = :profissional " +
           "AND b.ativo = true " +
           "AND b.dataHoraInicio >= :inicio AND b.dataHoraFim <= :fim")
    List<BloqueioAgenda> findBloqueiosPorProfissionalEPeriodo(
        @Param("profissional") AuthUsuario profissional,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
           "FROM BloqueioAgenda b WHERE b.profissionalUsuario = :profissional " +
           "AND b.ativo = true " +
           "AND ((b.dataHoraInicio <= :fim AND b.dataHoraFim >= :inicio))")
    boolean existsBloqueioNoHorario(
        @Param("profissional") AuthUsuario profissional,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT b FROM BloqueioAgenda b WHERE b.tipo = :tipo AND b.ativo = true")
    List<BloqueioAgenda> findByTipo(@Param("tipo") String tipo);
}
