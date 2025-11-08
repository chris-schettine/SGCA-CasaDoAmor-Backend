package br.com.casadoamor.sgca.modules.dadoClinico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.casadoamor.sgca.modules.dadoClinico.entity.DadoClinico;
import br.com.casadoamor.sgca.modules.dadoClinico.mapper.DadoClinicoMapper;
import br.com.casadoamor.sgca.modules.dadoClinico.repository.DadoClinicoRepository;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoClinicoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoClinicoInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoClinicoInputDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DadoClinicoServiceImp implements DadoClinicoService {

  private final DadoClinicoRepository dadoClinicoRepository;
  private final PacienteRepository pacienteRepository;
  private final DadoClinicoMapper dadoClinicoMapper;

  @Override
  @Transactional
  public DadoClinicoDTO criarDadoClinico(String pacienteId, DadoClinicoInputDTO dto) {
    Paciente paciente = pacienteRepository.findById(pacienteId)
        .orElseThrow(() -> new RuntimeException("Paciente não encontrado com ID: " + pacienteId));

    DadoClinico dadoClinico = dadoClinicoMapper.toEntity(dto);
    dadoClinico.setPaciente(paciente);

    DadoClinico savedDadoClinico = dadoClinicoRepository.save(dadoClinico);
    return dadoClinicoMapper.toDTO(savedDadoClinico);
  }

  @Override
  @Transactional
  public DadoClinicoDTO atualizarDadoClinico(String id, EditarDadoClinicoInputDTO dto) {
    DadoClinico dadoClinico = dadoClinicoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Dado clínico não encontrado com ID: " + id));

    dadoClinicoMapper.updateEntity(dadoClinico, dto);
    DadoClinico updatedDadoClinico = dadoClinicoRepository.save(dadoClinico);
    
    return dadoClinicoMapper.toDTO(updatedDadoClinico);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DadoClinicoDTO> buscarDadosClinicosPorPaciente(String pacienteId) {
    List<DadoClinico> dadosClinicos = dadoClinicoRepository.findByPacienteId(pacienteId);
    return dadosClinicos.stream()
        .map(dadoClinicoMapper::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public DadoClinicoDTO buscarDadoClinicoAtual(String pacienteId) {
    DadoClinico dadoClinico = dadoClinicoRepository.findFirstByPacienteIdOrderByCreatedAtDesc(pacienteId)
        .orElseThrow(() -> new RuntimeException("Nenhum dado clínico encontrado para o paciente: " + pacienteId));
    
    return dadoClinicoMapper.toDTO(dadoClinico);
  }

  @Override
  @Transactional(readOnly = true)
  public DadoClinicoDTO buscarPorId(String id) {
    DadoClinico dadoClinico = dadoClinicoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Dado clínico não encontrado com ID: " + id));
    
    return dadoClinicoMapper.toDTO(dadoClinico);
  }
}
