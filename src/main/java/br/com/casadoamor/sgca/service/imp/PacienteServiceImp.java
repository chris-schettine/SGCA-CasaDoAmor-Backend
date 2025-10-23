package br.com.casadoamor.sgca.service.imp;

import java.util.List;

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
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

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
    
    // Validação de campos obrigatórios
    if (registrarPacienteDTO.getDadoPessoal() == null) {
      throw new CustomError("Dado pessoal é obrigatório", HttpStatus.BAD_REQUEST);
    }

    if (registrarPacienteDTO.getEndereco() == null) {
      throw new CustomError("Endereço é obrigatório", HttpStatus.BAD_REQUEST);
    }

    if (pacienteRepository.existsByCpf(registrarPacienteDTO.getDadoPessoal().getCpf())) {
      throw new CustomError("CPF já cadastrado", HttpStatus.BAD_REQUEST);
    }

    if (pacienteRepository.existsByRg(registrarPacienteDTO.getDadoPessoal().getRg())) {
      throw new CustomError("RG já cadastrado", HttpStatus.BAD_REQUEST);
    }

    DadoPessoal dadoPessoal = dadoPessoalMapper.toEntity(registrarPacienteDTO.getDadoPessoal());

    Endereco endereco = enderecoMapper.toEntity(registrarPacienteDTO.getEndereco());

    Paciente paciente = pacienteMapper.toEntityFromEntities(dadoPessoal, endereco);

    pacienteRepository.save(paciente);

    return pacienteMapper.toDTO(paciente);
  }

  public PacienteDTO editarPaciente(String id, EditarPacienteDTO editarPacienteDTO) {

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
        if (dados.getNomeMae() != null) pacienteExistente.getDadoPessoal().setNomeMae(dados.getNomeMae());
        if (dados.getDataNascimento() != null) pacienteExistente.getDadoPessoal().setDataNascimento(dados.getDataNascimento());
        if (dados.getCpf() != null) pacienteExistente.getDadoPessoal().setCpf(dados.getCpf());
        if (dados.getRg() != null) pacienteExistente.getDadoPessoal().setRg(dados.getRg());
        if (dados.getNaturalidade() != null) pacienteExistente.getDadoPessoal().setNaturalidade(dados.getNaturalidade());
        if (dados.getProfissao() != null) pacienteExistente.getDadoPessoal().setProfissao(dados.getProfissao());
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
      String searchPlain = searchText.toLowerCase();
      String searchNorm = searchText.toLowerCase().replaceAll("\\s|\\.|-", "");

      var dadoJoin = root.join("dadoPessoal");

      // normalize DB cpf/rg once and use for starts/contains/exact
      var cpfNormalizedDb = criteriaBuilder.lower(
        criteriaBuilder.function("REPLACE", String.class,
          criteriaBuilder.function("REPLACE", String.class,
            criteriaBuilder.function("REPLACE", String.class,
              dadoJoin.get("cpf"),
              criteriaBuilder.literal("."),
              criteriaBuilder.literal("")
            ),
            criteriaBuilder.literal("-"),
            criteriaBuilder.literal("")
          ),
          criteriaBuilder.literal(" "),
          criteriaBuilder.literal("")
        )
      );

      var rgNormalizedDb = criteriaBuilder.lower(
        criteriaBuilder.function("REPLACE", String.class,
          criteriaBuilder.function("REPLACE", String.class,
            criteriaBuilder.function("REPLACE", String.class,
              dadoJoin.get("rg"),
              criteriaBuilder.literal("."),
              criteriaBuilder.literal("")
            ),
            criteriaBuilder.literal("-"),
            criteriaBuilder.literal("")
          ),
          criteriaBuilder.literal(" "),
          criteriaBuilder.literal("")
        )
      );

      // name predicates (using lower-cased name)
      Predicate nameContains = criteriaBuilder.like(criteriaBuilder.lower(dadoJoin.get("nome")), search);
      Predicate nameStarts = criteriaBuilder.like(criteriaBuilder.lower(dadoJoin.get("nome")), searchPlain + "%");
      Predicate exactName = criteriaBuilder.equal(criteriaBuilder.lower(dadoJoin.get("nome")), searchPlain);

      // cpf predicates (use normalized DB and normalized search)
      Predicate cpfContains = criteriaBuilder.like(cpfNormalizedDb, "%" + searchNorm + "%");
      Predicate cpfStarts = criteriaBuilder.like(cpfNormalizedDb, searchNorm + "%");
      Predicate exactCpf = criteriaBuilder.equal(cpfNormalizedDb, searchNorm);

      // rg predicates
      Predicate rgContains = criteriaBuilder.like(rgNormalizedDb, "%" + searchNorm + "%");
      Predicate rgStarts = criteriaBuilder.like(rgNormalizedDb, searchNorm + "%");
      Predicate exactRg = criteriaBuilder.equal(rgNormalizedDb, searchNorm);

      // overall contains predicate (keep to filter results)
      Predicate anyContains = criteriaBuilder.or(nameContains, cpfContains, rgContains);
      predicate = criteriaBuilder.and(predicate, anyContains);

      // CASE: exact (0), starts-with (1), contains (2), else 3
      var caseExpr = criteriaBuilder.selectCase()
        .when(criteriaBuilder.or(exactName, exactCpf, exactRg), 0)
        .when(criteriaBuilder.or(nameStarts, cpfStarts, rgStarts), 1)
        .when(criteriaBuilder.or(nameContains, cpfContains, rgContains), 2)
        .otherwise(3);

      query.orderBy(criteriaBuilder.asc(caseExpr), criteriaBuilder.asc(root.get("id")));
    } else {
      query.orderBy(criteriaBuilder.asc(root.get("id")));
    }
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
