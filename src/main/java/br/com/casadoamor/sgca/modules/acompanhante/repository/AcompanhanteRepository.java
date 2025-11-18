package br.com.casadoamor.sgca.modules.acompanhante.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

public interface AcompanhanteRepository extends JpaRepository<Acompanhante, String>, JpaSpecificationExecutor<Acompanhante> {
  List<Acompanhante> findByPacienteAndDeletedAtIsNull(Paciente paciente);

  @Query("""
       SELECT a FROM Acompanhante a
       JOIN a.dadoPessoal d
       WHERE 
          LOWER(REPLACE(REPLACE(REPLACE(d.cpf,'.',''),'-',''),' ','')) LIKE %:clean%
          OR LOWER(REPLACE(REPLACE(REPLACE(d.rg,'.',''),'-',''),' ','')) LIKE %:clean%
          OR LOWER(d.nome) LIKE %:termo%
          AND a.deletedAt IS NULL
       """)
  List<Acompanhante> buscarInteligente(@Param("termo") String termo, @Param("clean") String clean);

}
