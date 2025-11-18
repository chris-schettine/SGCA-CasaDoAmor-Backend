package br.com.casadoamor.sgca.modules.paciente.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.com.casadoamor.sgca.infra.config.exception.CustomError;
import br.com.casadoamor.sgca.infra.util.CpfUtil;
import br.com.casadoamor.sgca.infra.util.RgUtil;
import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.entity.DadoSocial;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.common.enums.PacienteStatus;
import br.com.casadoamor.sgca.modules.common.enums.SexoEnum;
import br.com.casadoamor.sgca.modules.common.mapper.DadoPessoalMapper;
import br.com.casadoamor.sgca.modules.common.mapper.DadoSocialMapper;
import br.com.casadoamor.sgca.modules.common.mapper.EnderecoMapper;
import br.com.casadoamor.sgca.modules.common.mapper.PaginatedResponseMapper;
import br.com.casadoamor.sgca.modules.common.repository.DadoPessoalRepository;
import br.com.casadoamor.sgca.modules.dadoClinico.entity.DadoClinico;
import br.com.casadoamor.sgca.modules.dadoClinico.mapper.DadoClinicoMapper;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.PacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarObitoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.RegistrarPacienteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.ContatoEmergencia;
import br.com.casadoamor.sgca.modules.paciente.entity.HistoricoPaciente;
import br.com.casadoamor.sgca.modules.paciente.entity.InformacaoHospitalar;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.entity.PoliticaPrivacidade;
import br.com.casadoamor.sgca.modules.paciente.mapper.ContatoEmergenciaMapper;
import br.com.casadoamor.sgca.modules.paciente.mapper.HistoricoPacienteMapper;
import br.com.casadoamor.sgca.modules.paciente.mapper.InformacaoHospitalarMapper;
import br.com.casadoamor.sgca.modules.paciente.mapper.ObituarioMapper;
import br.com.casadoamor.sgca.modules.paciente.mapper.PacienteMapper;
import br.com.casadoamor.sgca.modules.paciente.mapper.PoliticaPrivacidadeMapper;
import br.com.casadoamor.sgca.modules.paciente.repository.HistoricoPacienteRepository;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
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
  private final ObituarioMapper obituarioMapper;

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

        String rgLimpo = dados.getRg() != null ? RgUtil.limparRg(dados.getRg()) : null;

        if (rgLimpo != null &&
            !rgLimpo.equals(pacienteExistente.getDadoPessoal().getRg()) &&
            pacienteRepository.existsByRg(rgLimpo)) {
            throw new CustomError("RG já cadastrado", HttpStatus.BAD_REQUEST);
        }

        DadoPessoal dadoPessoalAtual = pacienteExistente.getDadoPessoal();
        DadoPessoal dadoPessoalAtualizado = dadoPessoalMapper.updateEntity(dadoPessoalAtual, dados);
        pacienteExistente.setDadoPessoal(dadoPessoalAtualizado);
    }

    if (editarPacienteDTO.getEndereco() != null) {
      var end = editarPacienteDTO.getEndereco();
      Endereco enderecoAtual = pacienteExistente.getEndereco();
      Endereco enderecoAtualizado = enderecoMapper.updateEntity(enderecoAtual, end);
      pacienteExistente.setEndereco(enderecoAtualizado);
    }

    if (editarPacienteDTO.getEmail() != null) {
      String emailLower = editarPacienteDTO.getEmail().toLowerCase();
      if (!emailLower.equals(pacienteExistente.getEmail()) &&
          pacienteRepository.existsByEmail(emailLower)) {
          throw new CustomError("Email já cadastrado", HttpStatus.BAD_REQUEST);
      }
      pacienteExistente.setEmail(emailLower);
    }

    if (editarPacienteDTO.getDadoSocial() != null) {
      var dadoSocialDTO = editarPacienteDTO.getDadoSocial();
      DadoSocial dadoSocial = pacienteExistente.getDadoSocial();

      if (dadoSocialDTO.getRendaFamiliar() != null) dadoSocial.setRendaFamiliar(dadoSocialDTO.getRendaFamiliar());
      if (dadoSocialDTO.getComposicaoFamiliar() != null) dadoSocial.setComposicaoFamiliar(dadoSocialDTO.getComposicaoFamiliar());
      if (dadoSocialDTO.getSituacaoMoradia() != null) dadoSocial.setSituacaoMoradia(dadoSocialDTO.getSituacaoMoradia());
      if (dadoSocialDTO.getNecessidadesEspeciais() != null) dadoSocial.setNecessidadesEspeciais(dadoSocialDTO.getNecessidadesEspeciais());
    }

    if (editarPacienteDTO.getInformacaoHospitalar() != null) {
      var infoDTO = editarPacienteDTO.getInformacaoHospitalar();
      InformacaoHospitalar info = pacienteExistente.getInformacaoHospitalar();

      if (infoDTO.getNomeHospitalReferencia() != null) info.setNomeHospitalReferencia(infoDTO.getNomeHospitalReferencia());
      if (infoDTO.getMedicoResponsavel() != null) info.setMedicoResponsavel(infoDTO.getMedicoResponsavel());
      if (infoDTO.getSetorAla() != null) info.setSetorAla(infoDTO.getSetorAla());
      if (infoDTO.getDataInternacao() != null) info.setDataInternacao(infoDTO.getDataInternacao());
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

  public  PaginatedResponseDTO<PacienteDTO> pacientesPaginados (
    String searchText, int limit, int offset,
    PacienteStatus status, String diagnostico, String hospitalReferencia,
    String dataCadastroInicio, String dataCadastroFim,
    Integer idadeMin, Integer idadeMax, SexoEnum genero,
    String cidade, String necessidadeEspecial
  ) {
     Specification<Paciente> spec = (root, query, criteriaBuilder) -> {
        Predicate predicate = criteriaBuilder.conjunction(); 

    if (status == null || status != PacienteStatus.INATIVO) {
      predicate = criteriaBuilder.and(predicate, criteriaBuilder.isNull(root.get("deletedAt")));
    }
    
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
    
    // STATUS (ativo, inativo, falecido)
    if (status != null) {
      switch (status) {
          case PacienteStatus.ATIVO:
              predicate = criteriaBuilder.and(
                  predicate,
                  criteriaBuilder.equal(root.get("status"), PacienteStatus.ATIVO)
              );
              break;

          case PacienteStatus.INATIVO:
              predicate = criteriaBuilder.and(
                  predicate,
                  criteriaBuilder.equal(root.get("status"), PacienteStatus.INATIVO)
              );
              break;

          case PacienteStatus.FALECIDO:
              predicate = criteriaBuilder.and(
                  predicate,
                  criteriaBuilder.equal(root.get("status"), PacienteStatus.FALECIDO)
              );
              break;
      }
    }

      // DIAGNÓSTICO
      if (diagnostico != null && !diagnostico.isBlank()) {
          Join<Paciente, DadoClinico> dadosClinicosJoin = root.join("dadosClinicos");
          predicate = criteriaBuilder.and(predicate,
              criteriaBuilder.like(criteriaBuilder.lower(dadosClinicosJoin.get("diagnostico")),
                      "%" + diagnostico.toLowerCase() + "%")
          );
      }

      // HOSPITAL DE REFERÊNCIA
      if (hospitalReferencia != null && !hospitalReferencia.isBlank()) {
          Join<Paciente, InformacaoHospitalar> hospitalJoin = root.join("informacaoHospitalar");
          predicate = criteriaBuilder.and(predicate,
              criteriaBuilder.like(criteriaBuilder.lower(hospitalJoin.get("nomeHospitalReferencia")),
                      "%" + hospitalReferencia.toLowerCase() + "%")
          );
      }

      // DATA CADASTRO
      if (dataCadastroInicio != null) {
          predicate = criteriaBuilder.and(predicate,
              criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"),
                  LocalDate.parse(dataCadastroInicio).atStartOfDay())
          );
      }

      if (dataCadastroFim != null) {
          predicate = criteriaBuilder.and(predicate,
              criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"),
                  LocalDate.parse(dataCadastroFim).atTime(23, 59, 59))
          );
      }

      // FAIXA ETÁRIA
      if (idadeMin != null || idadeMax != null) {
          Join<Paciente, DadoPessoal> joinDado = root.join("dadoPessoal");

          Expression<Integer> idade =
              criteriaBuilder.function("TIMESTAMPDIFF", Integer.class,
                  criteriaBuilder.literal("YEAR"),
                  joinDado.get("dataNascimento"),
                  criteriaBuilder.currentDate()
              );

          if (idadeMin != null) {
              predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(idade, idadeMin));
          }
          if (idadeMax != null) {
              predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(idade, idadeMax));
          }
      }

      // GÊNERO
      if (genero != null) {
          Join<Paciente, DadoPessoal> joinDado = root.join("dadoPessoal");
          predicate = criteriaBuilder.and(predicate,
              criteriaBuilder.equal(joinDado.get("sexo"), genero)
          );
      }

      // CIDADE
      if (cidade != null && !cidade.isBlank()) {
          Join<Paciente, Endereco> joinEndereco = root.join("endereco");
          predicate = criteriaBuilder.and(predicate,
              criteriaBuilder.like(criteriaBuilder.lower(joinEndereco.get("cidade")),
                      "%" + cidade.toLowerCase() + "%")
          );
      }

      // NECESSIDADE ESPECIAL
      if (necessidadeEspecial != null && !necessidadeEspecial.isBlank()) {
          Join<Paciente, DadoSocial> joinSocial = root.join("dadoSocial");
          predicate = criteriaBuilder.and(predicate,
              criteriaBuilder.like(criteriaBuilder.lower(joinSocial.get("necessidadesEspeciais")),
                      "%" + necessidadeEspecial.toLowerCase() + "%")
          );
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

  @Override
  public PaginatedResponseDTO<HistoricoPacienteDTO> historicoPacientePaginado(String pacienteId, int limit, int offset) {
    Paciente paciente = pacienteRepository.findById(pacienteId)
      .orElseThrow(() -> new CustomError("Paciente não encontrado", HttpStatus.NOT_FOUND));

    List<HistoricoPaciente> historicos = historicoRepository.findByPacienteOrderByDataRegistroDesc(paciente);

    int start = Math.min(offset, historicos.size());
    int end = Math.min(offset + limit, historicos.size());

    List<HistoricoPacienteDTO> nodes = historicos.subList(start, end)
      .stream()
      .map(historicoPacienteMapper::toHistoricoPacienteDTO)
      .toList();

    boolean hasPreviousPage = offset > 0;
    boolean hasNextPage = (offset + limit) < historicos.size();

    return paginatedMapper.toDTO(nodes, historicos.size(), hasPreviousPage, hasNextPage);
  }

  @Override
  @Transactional
  public void deletarPaciente(String id) {
    String deletedBy = SecurityContextHolder.getContext().getAuthentication().getName();

    Paciente paciente = pacienteRepository.findById(id)
      .orElseThrow(() -> new CustomError("Paciente não encontrado", HttpStatus.NOT_FOUND));

    if (paciente.isDeleted()) {
      throw new CustomError("Paciente já foi removido anteriormente", HttpStatus.BAD_REQUEST);
    }

    paciente.setStatus(PacienteStatus.INATIVO);

    paciente.markAsDeleted(deletedBy);

    pacienteRepository.save(paciente);
  }

  @Override
  @Transactional
  public PacienteDTO registrarObito(String id, RegistrarObitoDTO dto) {
    Paciente paciente = pacienteRepository.findById(id)
        .orElseThrow(() -> new CustomError("Paciente não encontrado", HttpStatus.NOT_FOUND));

    if (paciente.getStatus() == PacienteStatus.FALECIDO) {
        throw new CustomError("Óbito já registrado para este paciente.", HttpStatus.BAD_REQUEST);
    }

    // Atualiza status para falecido
    paciente.setStatus(PacienteStatus.FALECIDO);

    // Cria obituário
    var obituario = obituarioMapper.toEntity(dto);
    paciente.setObituario(obituario);

    pacienteRepository.save(paciente);

    HistoricoPaciente historico = historicoPacienteMapper.toEntity(
        paciente,
        null,
        "Óbito registrado para o paciente."
    );
    historicoRepository.save(historico);

    return pacienteMapper.toDTO(paciente);
  }

}
