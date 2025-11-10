package br.com.casadoamor.sgca.modules.paciente.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.paciente.entity.HistoricoPaciente;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;


public interface HistoricoPacienteRepository extends JpaRepository<HistoricoPaciente, String> {
  List<HistoricoPaciente> findByPacienteOrderByDataRegistroDesc(Paciente paciente);
  List<HistoricoPaciente> findByAcompanhanteOrderByDataRegistroDesc(Acompanhante acompanhante);
}
