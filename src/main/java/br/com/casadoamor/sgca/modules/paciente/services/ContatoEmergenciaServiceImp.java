package br.com.casadoamor.sgca.modules.paciente.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.ContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarContatoEmergenciaInputDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.ContatoEmergencia;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.mapper.ContatoEmergenciaMapper;
import br.com.casadoamor.sgca.modules.paciente.repository.ContatoEmergenciaRepository;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContatoEmergenciaServiceImp implements ContatoEmergenciaService {

  private final ContatoEmergenciaRepository contatoEmergenciaRepository;
  private final PacienteRepository pacienteRepository;
  private final ContatoEmergenciaMapper contatoEmergenciaMapper;

  @Override
  @Transactional
  public ContatoEmergenciaDTO criar(String pacienteId, ContatoEmergenciaInputDTO dto) {

    Paciente paciente = pacienteRepository.findById(pacienteId)
      .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

    ContatoEmergencia contato = contatoEmergenciaMapper.toEntity(dto, paciente);

    contato = contatoEmergenciaRepository.save(contato);

    return contatoEmergenciaMapper.toDTO(contato);
  }

  @Override
  public List<ContatoEmergenciaDTO> listarPorPaciente(String pacienteId) {
    List<ContatoEmergencia> contatos = contatoEmergenciaRepository.findByPacienteId(pacienteId);
    return contatoEmergenciaMapper.toDTOList(contatos);
  }

  @Override
  @Transactional
  public ContatoEmergenciaDTO atualizar(String id, EditarContatoEmergenciaInputDTO dto) {
    ContatoEmergencia contato = contatoEmergenciaRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Contato não encontrado"));

    contatoEmergenciaMapper.updateEntity(contato, dto);

    contatoEmergenciaRepository.save(contato);

    return contatoEmergenciaMapper.toDTO(contato);
  }

  @Override
  @Transactional
  public void remover(String id) {
    ContatoEmergencia contato = contatoEmergenciaRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Contato não encontrado"));

    contatoEmergenciaRepository.delete(contato);
  }
}
