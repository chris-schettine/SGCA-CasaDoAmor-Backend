package br.com.casadoamor.sgca.modules.paciente.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, String>, JpaSpecificationExecutor<Paciente> {
  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Paciente p WHERE p.dadoPessoal.cpf = :cpf")
  Boolean existsByCpf(@Param("cpf") String cpf);

  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Paciente p WHERE p.dadoPessoal.rg = :rg")
  Boolean existsByRg(@Param("rg") String rg);
  
  @Override
  @EntityGraph(attributePaths = {"dadoPessoal", "endereco"})
  @NonNull
  List<Paciente> findAll(@Nullable Specification<Paciente> spec);

  Optional<Paciente> findByEmail(String email);
}
