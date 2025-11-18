package br.com.casadoamor.sgca.modules.paciente.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.casadoamor.sgca.modules.paciente.entity.ContatoEmergencia;

public interface ContatoEmergenciaRepository extends JpaRepository<ContatoEmergencia, String> {
  List<ContatoEmergencia> findByPacienteId(String pacienteId);
}
