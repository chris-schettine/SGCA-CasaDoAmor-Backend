package br.com.casadoamor.sgca.modules.common.service.imp;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.acompanhante.mapper.AcompanhanteMapper;
import br.com.casadoamor.sgca.modules.acompanhante.repository.AcompanhanteRepository;
import br.com.casadoamor.sgca.modules.common.dto.BuscaInteligenteResponseDTO;
import br.com.casadoamor.sgca.modules.common.service.BuscaInteligenteService;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.mapper.PacienteMapper;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuscaInteligenteServiceImp implements BuscaInteligenteService {

  private final PacienteRepository pacienteRepository;
  private final AcompanhanteRepository acompanhanteRepository;
  private final PacienteMapper pacienteMapper;
  private final AcompanhanteMapper acompanhanteMapper;

  @Override
  public BuscaInteligenteResponseDTO buscar(String termo) {
    String termoLimpo = termo.toLowerCase().replaceAll("[^a-z0-9]", "");

    List<Paciente> pacientes = pacienteRepository.buscarInteligente(termo, termoLimpo);

    List<PacienteDTO> pacientesDto = pacienteMapper.toListDTO(pacientes);

    List<Acompanhante> acompanhantes = acompanhanteRepository.buscarInteligente(termo, termoLimpo);

    List<AcompanhanteDTO> acompanhantesDto = acompanhanteMapper.toListDTO(acompanhantes);

    return BuscaInteligenteResponseDTO.builder()
      .pacientes(pacientesDto)
      .acompanhantes(acompanhantesDto)
      .build();
  }
}
