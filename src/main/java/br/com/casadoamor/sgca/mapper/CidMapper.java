package br.com.casadoamor.sgca.mapper;

import br.com.casadoamor.sgca.dto.CidDto;
import br.com.casadoamor.sgca.entity.Cid;

public class CidMapper {

    public static CidDto toCidDto(Cid cid) {
        return new CidDto(
                cid.getSubcat(),
                cid.getDescricao(),
                cid.getDescrabrev()
        );
    }
}
