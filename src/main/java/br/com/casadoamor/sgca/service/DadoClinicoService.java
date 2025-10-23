package br.com.casadoamor.sgca.service;

import br.com.casadoamor.sgca.dto.paciente.DadoClinicoDTO;
import br.com.casadoamor.sgca.dto.paciente.DadoClinicoInputDTO;

import java.util.List;

public interface DadoClinicoService {
  
  /**
   * Cria um novo registro de dado clínico para um paciente
   * @param pacienteId ID do paciente
   * @param dto Dados clínicos a serem registrados
   * @return DTO com os dados clínicos criados
   */
  DadoClinicoDTO criarDadoClinico(String pacienteId, DadoClinicoInputDTO dto);
  
  /**
   * Atualiza um registro de dado clínico existente
   * @param id ID do dado clínico
   * @param dto Dados clínicos a serem atualizados
   * @return DTO com os dados clínicos atualizados
   */
  DadoClinicoDTO atualizarDadoClinico(String id, DadoClinicoInputDTO dto);
  
  /**
   * Busca todos os registros de dados clínicos de um paciente
   * @param pacienteId ID do paciente
   * @return Lista de DTOs com histórico de dados clínicos
   */
  List<DadoClinicoDTO> buscarDadosClinicosPorPaciente(String pacienteId);
  
  /**
   * Busca o registro mais recente de dados clínicos de um paciente
   * @param pacienteId ID do paciente
   * @return DTO com os dados clínicos mais recentes
   */
  DadoClinicoDTO buscarDadoClinicoAtual(String pacienteId);
  
  /**
   * Busca um dado clínico específico por ID
   * @param id ID do dado clínico
   * @return DTO com os dados clínicos
   */
  DadoClinicoDTO buscarPorId(String id);
}
