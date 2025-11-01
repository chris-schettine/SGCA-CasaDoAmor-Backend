package br.com.casadoamor.sgca.modules.acompanhante.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;

public interface AcompanhanteRepository extends JpaRepository<Acompanhante, String>, JpaSpecificationExecutor<Acompanhante> {
}
