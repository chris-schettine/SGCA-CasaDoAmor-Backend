package br.com.casadoamor.sgca.repository;

import br.com.casadoamor.sgca.entity.Paciente;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PacienteRepository extends JpaRepository<Paciente, UUID>, JpaSpecificationExecutor<Paciente> {
  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Paciente p WHERE p.dadoPessoal.cpf = :cpf")
  Boolean existsByCpf(@Param("cpf") String cpf);

  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Paciente p WHERE p.dadoPessoal.rg = :rg")
  Boolean existsByRg(@Param("rg") String rg);
}
