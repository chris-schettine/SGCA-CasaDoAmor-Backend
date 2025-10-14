package br.com.casadoamor.sgca.repository;

import br.com.casadoamor.sgca.entity.ProfissionalSaude;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, UUID> {

}
