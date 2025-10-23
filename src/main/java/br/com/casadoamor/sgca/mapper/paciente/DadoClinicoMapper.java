package br.com.casadoamor.sgca.mapper.paciente;

import br.com.casadoamor.sgca.dto.paciente.DadoClinicoDTO;
import br.com.casadoamor.sgca.dto.paciente.DadoClinicoInputDTO;
import br.com.casadoamor.sgca.entity.paciente.DadoClinico;
import org.springframework.stereotype.Component;

@Component
public class DadoClinicoMapper {

  public DadoClinico toEntity(DadoClinicoInputDTO dto) {
    DadoClinico entity = new DadoClinico();
    entity.setDiagnostico(dto.getDiagnostico());
    entity.setTratamento(dto.getTratamento());
    entity.setTratamentoOutroDescricao(dto.getTratamentoOutroDescricao());
    entity.setCondicaoChegada(dto.getCondicaoChegada());
    entity.setUsaSonda(dto.getUsaSonda());
    entity.setTipoSondaNasal(dto.getTipoSondaNasal());
    entity.setTipoSondaCirurgica(dto.getTipoSondaCirurgica());
    entity.setTipoSondaVesical(dto.getTipoSondaVesical());
    entity.setSondaOutraDescricao(dto.getSondaOutraDescricao());
    entity.setUsaCurativo(dto.getUsaCurativo());
    entity.setUsaOxigenoterapia(dto.getUsaOxigenoterapia());
    return entity;
  }

  public DadoClinicoDTO toDTO(DadoClinico entity) {
    return DadoClinicoDTO.builder()
        .id(entity.getId())
        .diagnostico(entity.getDiagnostico())
        .tratamento(entity.getTratamento())
        .tratamentoOutroDescricao(entity.getTratamentoOutroDescricao())
        .condicaoChegada(entity.getCondicaoChegada())
        .usaSonda(entity.getUsaSonda())
        .tipoSondaNasal(entity.getTipoSondaNasal())
        .tipoSondaCirurgica(entity.getTipoSondaCirurgica())
        .tipoSondaVesical(entity.getTipoSondaVesical())
        .sondaOutraDescricao(entity.getSondaOutraDescricao())
        .usaCurativo(entity.getUsaCurativo())
        .usaOxigenoterapia(entity.getUsaOxigenoterapia())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  public void updateEntity(DadoClinico entity, DadoClinicoInputDTO dto) {
    if (dto.getDiagnostico() != null) {
      entity.setDiagnostico(dto.getDiagnostico());
    }
    if (dto.getTratamento() != null) {
      entity.setTratamento(dto.getTratamento());
    }
    if (dto.getTratamentoOutroDescricao() != null) {
      entity.setTratamentoOutroDescricao(dto.getTratamentoOutroDescricao());
    }
    if (dto.getCondicaoChegada() != null) {
      entity.setCondicaoChegada(dto.getCondicaoChegada());
    }
    if (dto.getUsaSonda() != null) {
      entity.setUsaSonda(dto.getUsaSonda());
    }
    if (dto.getTipoSondaNasal() != null) {
      entity.setTipoSondaNasal(dto.getTipoSondaNasal());
    }
    if (dto.getTipoSondaCirurgica() != null) {
      entity.setTipoSondaCirurgica(dto.getTipoSondaCirurgica());
    }
    if (dto.getTipoSondaVesical() != null) {
      entity.setTipoSondaVesical(dto.getTipoSondaVesical());
    }
    if (dto.getSondaOutraDescricao() != null) {
      entity.setSondaOutraDescricao(dto.getSondaOutraDescricao());
    }
    if (dto.getUsaCurativo() != null) {
      entity.setUsaCurativo(dto.getUsaCurativo());
    }
    if (dto.getUsaOxigenoterapia() != null) {
      entity.setUsaOxigenoterapia(dto.getUsaOxigenoterapia());
    }
  }
}
