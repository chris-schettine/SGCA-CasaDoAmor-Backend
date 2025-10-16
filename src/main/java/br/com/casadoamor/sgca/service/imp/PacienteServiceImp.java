package br.com.casadoamor.sgca.service.imp;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.casadoamor.sgca.config.exception.CustomError;
import br.com.casadoamor.sgca.dto.common.PaginatedResponseDTO;
import br.com.casadoamor.sgca.dto.paciente.EditarPacienteDTO;
import br.com.casadoamor.sgca.dto.paciente.PacienteDTO;
import br.com.casadoamor.sgca.dto.paciente.RegistrarPacienteDTO;
import br.com.casadoamor.sgca.entity.paciente.DadoPessoal;
import br.com.casadoamor.sgca.entity.paciente.Endereco;
import br.com.casadoamor.sgca.entity.paciente.Paciente;
import br.com.casadoamor.sgca.mapper.common.PaginatedResponseMapper;
import br.com.casadoamor.sgca.mapper.paciente.DadoPessoalMapper;
import br.com.casadoamor.sgca.mapper.paciente.EnderecoMapper;
import br.com.casadoamor.sgca.mapper.paciente.PacienteMapper;
import br.com.casadoamor.sgca.repository.paciente.PacienteRepository;
import br.com.casadoamor.sgca.service.paciente.PacienteService;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
public class PacienteServiceImp implements PacienteService {
  private final PacienteRepository pacienteRepository;
  private final EnderecoMapper enderecoMapper;
  private final DadoPessoalMapper dadoPessoalMapper;
  private final PacienteMapper pacienteMapper;
  private final PaginatedResponseMapper paginatedMapper;

  @Override
  public PacienteDTO registrarPaciente(RegistrarPacienteDTO registrarPacienteDTO) {
    
    if (pacienteRepository.existsByCpf(registrarPacienteDTO.dadoPessoal().cpf())) {
      throw new CustomError("CPF já cadastrado", HttpStatus.BAD_REQUEST);
    }

    if (pacienteRepository.existsByRg(registrarPacienteDTO.dadoPessoal().rg())) {
      throw new CustomError("RG já cadastrado", HttpStatus.BAD_REQUEST);
    }

    DadoPessoal dadoPessoal = dadoPessoalMapper.toEntity(registrarPacienteDTO.dadoPessoal());

    Endereco endereco = enderecoMapper.toEntity(registrarPacienteDTO.endereco());

    Paciente paciente = pacienteMapper.toEntityFromEntities(dadoPessoal, endereco);

    pacienteRepository.save(paciente);

    return pacienteMapper.toDTO(paciente);
  }

  public PacienteDTO editarPaciente(UUID id, EditarPacienteDTO editarPacienteDTO) {

    Paciente pacienteExistente = pacienteRepository.findById(id)
      .orElseThrow(() -> new CustomError("Paciente não encontrado", HttpStatus.NOT_FOUND));

    if (editarPacienteDTO.getDadoPessoal() != null) {
        var dados = editarPacienteDTO.getDadoPessoal();

        if (dados.getCpf() != null &&
            !dados.getCpf().equals(pacienteExistente.getDadoPessoal().getCpf()) &&
            pacienteRepository.existsByCpf(dados.getCpf())) {
            throw new CustomError("CPF já cadastrado", HttpStatus.BAD_REQUEST);
        }

        if (dados.getRg() != null &&
            !dados.getRg().equals(pacienteExistente.getDadoPessoal().getRg()) &&
            pacienteRepository.existsByRg(dados.getRg())) {
            throw new CustomError("RG já cadastrado", HttpStatus.BAD_REQUEST);
        }

        if (dados.getNome() != null) pacienteExistente.getDadoPessoal().setNome(dados.getNome());
        if (dados.getDataNascimento() != null) pacienteExistente.getDadoPessoal().setDataNascimento(dados.getDataNascimento());
        if (dados.getCpf() != null) pacienteExistente.getDadoPessoal().setCpf(dados.getCpf());
        if (dados.getRg() != null) pacienteExistente.getDadoPessoal().setRg(dados.getRg());
        if (dados.getNaturalidade() != null) pacienteExistente.getDadoPessoal().setNaturalidade(dados.getNaturalidade());
        if (dados.getTelefone() != null) pacienteExistente.getDadoPessoal().setTelefone(dados.getTelefone());
    }

    if (editarPacienteDTO.getEndereco() != null) {
        var end = editarPacienteDTO.getEndereco();

        if (end.getLogradouro() != null) pacienteExistente.getEndereco().setLogradouro(end.getLogradouro());
        if (end.getNumero() != null) pacienteExistente.getEndereco().setNumero(end.getNumero());
        if (end.getComplemento() != null) pacienteExistente.getEndereco().setComplemento(end.getComplemento());
        if (end.getBairro() != null) pacienteExistente.getEndereco().setBairro(end.getBairro());
        if (end.getCidade() != null) pacienteExistente.getEndereco().setCidade(end.getCidade());
        if (end.getEstado() != null) pacienteExistente.getEndereco().setEstado(end.getEstado());
        if (end.getCep() != null) pacienteExistente.getEndereco().setCep(end.getCep());
    }

    pacienteRepository.save(pacienteExistente);

    return pacienteMapper.toDTO(pacienteExistente);
  }

  public  PaginatedResponseDTO<PacienteDTO> pacientesPaginados (String searchText, int limit, int offset) {
     Specification<Paciente> spec = (root, query, criteriaBuilder) -> {
        Predicate predicate = criteriaBuilder.conjunction(); 

        if (searchText != null && !searchText.isBlank()) {
            String search = "%" + searchText.toLowerCase() + "%";

            var dadoJoin = root.join("dadoPessoal");

            Predicate matchNome = criteriaBuilder.like(criteriaBuilder.lower(dadoJoin.get("nome")), search);
            Predicate matchCpf = criteriaBuilder.like(criteriaBuilder.lower(dadoJoin.get("cpf")), search);
            Predicate matchRg = criteriaBuilder.like(criteriaBuilder.lower(dadoJoin.get("rg")), search);

            predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(matchNome, matchCpf, matchRg));
        }

        query.orderBy(criteriaBuilder.asc(root.get("id"))); 
        return predicate;
    };

    List<Paciente> allPacientes = pacienteRepository.findAll(spec);

    int start = Math.min(offset, allPacientes.size());
    int end = Math.min(offset + limit, allPacientes.size());

    List<PacienteDTO> nodes = allPacientes.subList(start, end)
      .stream()
      .map(pacienteMapper::toDTO)
      .toList();

    boolean hasPreviousPage = offset > 0;
    boolean hasNextPage = (offset + limit) < allPacientes.size();

    return paginatedMapper.toDTO(nodes, allPacientes.size(), hasPreviousPage, hasNextPage);
  }

}
