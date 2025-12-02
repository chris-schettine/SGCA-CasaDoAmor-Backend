package br.com.casadoamor.sgca.modules.agendamento.repository;

import br.com.casadoamor.sgca.modules.agendamento.entity.ProfissionalEspecialidade;
import br.com.casadoamor.sgca.modules.agendamento.entity.enums.EspecialidadeProfissional;
import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfissionalEspecialidadeRepository extends JpaRepository<ProfissionalEspecialidade, Long> {

    List<ProfissionalEspecialidade> findByProfissionalUsuario(AuthUsuario profissionalUsuario);

    List<ProfissionalEspecialidade> findByCategoriaServico(EspecialidadeProfissional categoriaServico);

    @Query("SELECT p FROM ProfissionalEspecialidade p WHERE p.profissionalUsuario = :profissional " +
           "AND p.categoriaServico = :especialidade AND p.ativo = true")
    Optional<ProfissionalEspecialidade> findByProfissionalAndEspecialidade(
        @Param("profissional") AuthUsuario profissional,
        @Param("especialidade") EspecialidadeProfissional especialidade
    );

    @Query("SELECT p FROM ProfissionalEspecialidade p WHERE p.ativo = true")
    List<ProfissionalEspecialidade> findAllAtivos();

    @Query("SELECT p FROM ProfissionalEspecialidade p WHERE p.categoriaServico = :especialidade " +
           "AND p.ativo = true")
    List<ProfissionalEspecialidade> findProfissionaisPorEspecialidade(
        @Param("especialidade") EspecialidadeProfissional especialidade
    );

    boolean existsByProfissionalUsuarioAndCategoriaServicoAndAtivoTrue(
        AuthUsuario profissionalUsuario,
        EspecialidadeProfissional categoriaServico
    );
}
