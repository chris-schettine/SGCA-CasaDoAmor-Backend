package br.com.casadoamor.sgca.modules.common.repository;

import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository para gerenciamento de Endereços
 */
@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, String> {
    // Métodos herdados de JpaRepository já incluem findById(String id)
}
