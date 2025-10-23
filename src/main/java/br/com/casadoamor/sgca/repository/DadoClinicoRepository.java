package br.com.casadoamor.sgca.repository;

import br.com.casadoamor.sgca.entity.paciente.DadoClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DadoClinicoRepository extends JpaRepository<DadoClinico, String> {
  
  /**
   * Busca todos os dados clínicos de um paciente específico
   */
  List<DadoClinico> findByPacienteId(String pacienteId);
  
  /**
   * Busca o dado clínico mais recente de um paciente
   */
  Optional<DadoClinico> findFirstByPacienteIdOrderByCreatedAtDesc(String pacienteId);
}
