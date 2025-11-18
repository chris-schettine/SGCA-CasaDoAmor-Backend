package br.com.casadoamor.sgca.modules.paciente.mapper;

import org.springframework.stereotype.Component;

import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarObitoDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.Obituario;

@Component
public class ObituarioMapper {
  public Obituario toEntity (RegistrarObitoDTO dto) {
    if (dto == null) {
      return null;
    }

    Obituario obituario = new Obituario();
    if (dto.getDataHoraObito() != null) {
      obituario.setDataHoraObito(dto.getDataHoraObito());
    }
    if (dto.getLocalObito() != null) {
      obituario.setLocalObito(dto.getLocalObito());
    }
    if (dto.getCausaObito() != null) {
      obituario.setCausaObito(dto.getCausaObito());
    }
    if (dto.getNumeroDeclaracaoObito() != null) {
      obituario.setNumeroDeclaracaoObito(dto.getNumeroDeclaracaoObito());
    }
    if (dto.getObservacoes() != null) {
      obituario.setObservacoesObito(dto.getObservacoes());
    }
    if (dto.getResponsavelComunicacao() != null) {
      obituario.setResponsavelComunicacaoObito(dto.getResponsavelComunicacao());
    }

    return obituario;
  }
}
