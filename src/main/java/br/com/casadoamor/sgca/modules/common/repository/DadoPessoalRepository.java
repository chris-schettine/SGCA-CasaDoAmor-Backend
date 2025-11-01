package br.com.casadoamor.sgca.modules.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;

public interface DadoPessoalRepository extends JpaRepository<DadoPessoal, String> {
  Optional<DadoPessoal> findByCpf(String cpf);
  Optional<DadoPessoal> findByRg(String rg);
} 
