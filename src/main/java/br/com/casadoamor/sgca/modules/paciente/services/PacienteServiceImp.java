package br.com.casadoamor.sgca.modules.paciente.services;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.casadoamor.sgca.infra.config.exception.CustomError;
import br.com.casadoamor.sgca.infra.util.CpfUtil;
import br.com.casadoamor.sgca.infra.util.RgUtil;
import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.entity.DadoSocial;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.common.mapper.DadoPessoalMapper;
import br.com.casadoamor.sgca.modules.common.mapper.DadoSocialMapper;
import br.com.casadoamor.sgca.modules.common.mapper.EnderecoMapper;
import br.com.casadoamor.sgca.modules.common.mapper.PaginatedResponseMapper;
import br.com.casadoamor.sgca.modules.common.repository.DadoPessoalRepository;
import br.com.casadoamor.sgca.modules.dadoClinico.entity.DadoClinico;
import br.com.casadoamor.sgca.modules.dadoClinico.mapper.DadoClinicoMapper;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.ContatoEmergencia;
import br.com.casadoamor.sgca.modules.paciente.entity.HistoricoPaciente;
import br.com.casadoamor.sgca.modules.paciente.entity.InformacaoHospitalar;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.entity.PoliticaPrivacidade;
import br.com.casadoamor.sgca.modules.paciente.mapper.ContatoEmergenciaMapper;
import br.com.casadoamor.sgca.modules.paciente.mapper.HistoricoPacienteMapper;
import br.com.casadoamor.sgca.modules.paciente.mapper.InformacaoHospitalarMapper;
import br.com.casadoamor.sgca.modules.paciente.mapper.PacienteMapper;
import br.com.casadoamor.sgca.modules.paciente.mapper.PoliticaPrivacidadeMapper;
import br.com.casadoamor.sgca.modules.paciente.repository.HistoricoPacienteRepository;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PacienteServiceImp implements PacienteService {
  private final DadoPessoalRepository dadoPessoalRepository;
  private final EnderecoMapper enderecoMapper;
  private final DadoPessoalMapper dadoPessoalMapper;
  private final PacienteMapper pacienteMapper;
  private final PaginatedResponseMapper paginatedMapper;
  private final PacienteRepository pacienteRepository;
  private final HistoricoPacienteMapper historicoPacienteMapper;
  private final HistoricoPacienteRepository historicoRepository;
  private final ContatoEmergenciaMapper contatoEmergenciaMapper;
  private final DadoSocialMapper dadoSocialMapper;
  private final PoliticaPrivacidadeMapper politicaPrivacidadeMapper;
  private final DadoClinicoMapper dadoClinicoMapper;
  private final InformacaoHospitalarMapper informacaoHospitalarMapper;

  @Override
  @Transactional
  public PacienteDTO registrarPaciente(RegistrarPacienteDTO registrarPacienteDTO) {

    validarPacienteExistente(registrarPacienteDTO);

    DadoPessoal dadoPessoal = dadoPessoalMapper.toEntity(registrarPacienteDTO.getDadoPessoal());
    Endereco endereco = enderecoMapper.toEntity(registrarPacienteDTO.getEndereco());
    Paciente paciente = pacienteMapper.toEntityFromEntities(dadoPessoal, endereco);
    paciente.setEmail(registrarPacienteDTO.getEmail().toLowerCase());
    pacienteRepository.save(paciente);

    DadoClinico dadoClinico = dadoClinicoMapper.toEntity(registrarPacienteDTO.getDadoClinico());
    dadoClinico.setPaciente(paciente);
    paciente.getDadosClinicos().add(dadoClinico);

    PoliticaPrivacidade politicaPrivacidade = politicaPrivacidadeMapper.aceitouPolitica();
    paciente.setPoliticaPrivacidade(politicaPrivacidade);

    DadoSocial dadoSocial = dadoSocialMapper.toEntity(registrarPacienteDTO.getDadoSocial());
    paciente.setDadoSocial(dadoSocial);

    List<ContatoEmergencia> contatos = contatoEmergenciaMapper.toEntityList(registrarPacienteDTO.getContatosDeEmergencia(), paciente);
    paciente.setContatosEmergencia(contatos);
    
    InformacaoHospitalar informacaoHospitalar = informacaoHospitalarMapper.toEntity(registrarPacienteDTO.getInformacaoHospitalar());
    paciente.setInformacaoHospitalar(informacaoHospitalar);
    
    pacienteRepository.save(paciente);

    HistoricoPaciente historicoPaciente = historicoPacienteMapper.toEntity(
      paciente, 
      null,
      "Registro inicial do paciente"
    );

    historicoRepository.save(historicoPaciente);

    return pacienteMapper.toDTO(paciente);
  }

  private void validarPacienteExistente(RegistrarPacienteDTO registrarPacienteDTO) {
    String cpfLimpo = CpfUtil.limparCpf(registrarPacienteDTO.getDadoPessoal().getCpf());

    dadoPessoalRepository.findByCpf(cpfLimpo).ifPresent(dadoPessoal -> {
      throw new CustomError("CPF já cadastrado no sistema", HttpStatus.BAD_REQUEST);
    });

    String rgLimpo = RgUtil.limparRg(registrarPacienteDTO.getDadoPessoal().getRg());

    dadoPessoalRepository.findByRg(rgLimpo).ifPresent(dadoPessoal -> {
      throw new CustomError("RG já cadastrado no sistema", HttpStatus.BAD_REQUEST);
    });

    String email = registrarPacienteDTO.getEmail().toLowerCase();

    pacienteRepository.findByEmail(email).ifPresent(paciente -> {
      throw new CustomError("Email já cadastrado no sistema", HttpStatus.BAD_REQUEST);
    });

    if (registrarPacienteDTO.getDadoPessoal().getDataNascimento().isAfter(java.time.LocalDate.now())) {
      throw new CustomError("Data de nascimento não pode ser no futuro", HttpStatus.BAD_REQUEST);
    }
  }

  public PacienteDTO editarPaciente(String id, EditarPacienteDTO editarPacienteDTO) {

    Paciente pacienteExistente = pacienteRepository.findById(id)
      .orElseThrow(() -> new CustomError("Paciente não encontrado", HttpStatus.NOT_FOUND));

    if (editarPacienteDTO.getDadoPessoal() != null) {
        var dados = editarPacienteDTO.getDadoPessoal();
        
        // Limpa CPF se fornecido
        String cpfLimpo = dados.getCpf() != null ? CpfUtil.limparCpf(dados.getCpf()) : null;

        if (cpfLimpo != null &&
            !cpfLimpo.equals(pacienteExistente.getDadoPessoal().getCpf()) &&
            pacienteRepository.existsByCpf(cpfLimpo)) {
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
        if (cpfLimpo != null) pacienteExistente.getDadoPessoal().setCpf(cpfLimpo);
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

    HistoricoPaciente historicoPaciente = historicoPacienteMapper.toEntity(
      pacienteExistente, 
      null,
      "Dados do paciente editados."
    );

    historicoRepository.save(historicoPaciente);

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

      if (query != null) {
        query.orderBy(criteriaBuilder.asc(caseExpr), criteriaBuilder.asc(root.get("id")));
      }
      
    } else {
      if (query != null) {
        query.orderBy(criteriaBuilder.asc(root.get("id")));
      }
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
