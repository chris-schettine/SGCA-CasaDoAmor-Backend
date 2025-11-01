package br.com.casadoamor.sgca.modules.paciente.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.casadoamor.sgca.modules.paciente.entity.HistoricoPaciente;


public interface HistoricoPacienteRepository extends JpaRepository<HistoricoPaciente, UUID> {}
