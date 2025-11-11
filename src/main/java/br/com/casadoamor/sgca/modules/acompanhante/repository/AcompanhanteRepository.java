package br.com.casadoamor.sgca.modules.acompanhante.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;

public interface AcompanhanteRepository extends JpaRepository<Acompanhante, String>, JpaSpecificationExecutor<Acompanhante> {
  List<Acompanhante> findByPacienteAndDeletedAtIsNull(Paciente paciente);
}
