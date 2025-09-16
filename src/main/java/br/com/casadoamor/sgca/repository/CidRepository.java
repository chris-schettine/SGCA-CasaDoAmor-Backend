package br.com.casadoamor.sgca.repository;

import br.com.casadoamor.sgca.dto.CidDto;
import br.com.casadoamor.sgca.entity.Cid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CidRepository extends JpaRepository<Cid, Long> {
    List<Cid> findCidBySubcatContaining(String subcat);
    List<Cid> findCidByDescricaoContaining(String descricao);
}
