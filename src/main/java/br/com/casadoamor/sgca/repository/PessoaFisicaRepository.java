package br.com.casadoamor.sgca.repository;

import br.com.casadoamor.sgca.entity.PessoaFisica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PessoaFisicaRepository extends JpaRepository<PessoaFisica, Long> {
    Optional<PessoaFisica> findByCpf(String cpf);
    List<PessoaFisica> findByNomeContaining(String nome);
}
