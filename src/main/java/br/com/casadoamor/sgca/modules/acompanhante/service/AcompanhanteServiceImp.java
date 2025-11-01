package br.com.casadoamor.sgca.modules.acompanhante.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.casadoamor.sgca.infra.config.exception.CustomError;
import br.com.casadoamor.sgca.infra.util.CpfUtil;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.EditarAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.RegistrarAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.acompanhante.mapper.AcompanhanteMapper;
import br.com.casadoamor.sgca.modules.acompanhante.repository.AcompanhanteRepository;
import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.common.mapper.DadoPessoalMapper;
import br.com.casadoamor.sgca.modules.common.mapper.EnderecoMapper;
import br.com.casadoamor.sgca.modules.common.mapper.PaginatedResponseMapper;
import br.com.casadoamor.sgca.modules.common.repository.DadoPessoalRepository;
import br.com.casadoamor.sgca.modules.paciente.entity.HistoricoPaciente;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.mapper.HistoricoPacienteMapper;
import br.com.casadoamor.sgca.modules.paciente.repository.HistoricoPacienteRepository;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcompanhanteServiceImp implements AcompanhanteService {

  private final AcompanhanteRepository acompanhanteRepository;
  private final PacienteRepository pacienteRepository;
  private final HistoricoPacienteRepository historicoRepository;
  private final AcompanhanteMapper acompanhanteMapper;
  private final HistoricoPacienteMapper historicoPacienteMapper;
  private final DadoPessoalRepository dadoPessoalRepository;
  private final DadoPessoalMapper dadoPessoalMapper;
  private final EnderecoMapper enderecoMapper;
  private final PaginatedResponseMapper paginatedMapper;

  @Override
  @Transactional
  public AcompanhanteDTO registrarAcompanhante(RegistrarAcompanhanteDTO dto) {
    Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
      .orElseThrow(() ->  new CustomError("Paciente não encontrado", HttpStatus.NOT_FOUND));

    String cpfLimpo = CpfUtil.limparCpf(dto.getDadoPessoal().getCpf());

    dadoPessoalRepository.findByCpf(cpfLimpo).ifPresent(dadoPessoal -> {
      throw new CustomError("CPF já cadastrado no sistema", HttpStatus.BAD_REQUEST);
    });

    dadoPessoalRepository.findByRg(dto.getDadoPessoal().getRg()).ifPresent(dadoPessoal -> {
      throw new CustomError("RG já cadastrado no sistema", HttpStatus.BAD_REQUEST);
    });

    Acompanhante acompanhante = acompanhanteMapper.toEntity(dto, paciente);

    acompanhanteRepository.save(acompanhante);

    HistoricoPaciente historicoPaciente = historicoPacienteMapper.toEntity(
      paciente, 
      acompanhante,
      "Acompanhante " + acompanhante.getDadoPessoal().getNome() + " registrado."
    );

    historicoRepository.save(historicoPaciente);

    return acompanhanteMapper.mapToDTO(acompanhante);
  }

  @Override
  @Transactional
  public AcompanhanteDTO editarAcompanhante(String id, EditarAcompanhanteDTO dto) {
    Acompanhante acompanhante = acompanhanteRepository.findById(id)
      .orElseThrow(() -> new CustomError("Acompanhante não encontrado", HttpStatus.NOT_FOUND));

    if (dto.getPodeAjudarNaCozinha() != null)
      acompanhante.setPodeAjudarNaCozinha(dto.getPodeAjudarNaCozinha());

    if (dto.getDadoPessoal() != null) {
      
      String novoCpf = dto.getDadoPessoal().getCpf();
      String cpfLimpo = CpfUtil.limparCpf(novoCpf);

      if (!cpfLimpo.equals(acompanhante.getDadoPessoal().getCpf())) {
        dadoPessoalRepository.findByCpf(cpfLimpo).ifPresent(dadoPessoal -> {
          throw new CustomError("CPF já cadastrado no sistema", HttpStatus.BAD_REQUEST);
        });
      }

      DadoPessoal dadoPessoalAtualizado = dadoPessoalMapper.toEntity(dto.getDadoPessoal());
      acompanhante.setDadoPessoal(dadoPessoalAtualizado);
    }

    if (dto.getEndereco() != null) {
      Endereco endereco = enderecoMapper.toEntity(dto.getEndereco());
      acompanhante.setEndereco(endereco);
    }

    if (dto.getParentesco() != null)
      acompanhante.setParentesco(dto.getParentesco());

    if (dto.getAtivo() != null)
      acompanhante.setAtivo(dto.getAtivo());

    acompanhanteRepository.save(acompanhante);

    HistoricoPaciente historicoPaciente = historicoPacienteMapper.toEntity(
      acompanhante.getPaciente(), 
      acompanhante,
      "Acompanhante " + acompanhante.getDadoPessoal().getNome() + " editado."
    );

    historicoRepository.save(historicoPaciente);

    return acompanhanteMapper.mapToDTO(acompanhante);
  }

  @Override
  public PaginatedResponseDTO<AcompanhanteDTO> acompanhantesPaginados(String searchText, int limit, int offset) {
    Specification<Acompanhante> spec = (root, query, criteriaBuilder) -> {
      Predicate predicate = criteriaBuilder.conjunction();

      if (searchText != null && !searchText.isBlank()) {
        String search = "%" + searchText.toLowerCase() + "%";
        String searchPlain = searchText.toLowerCase();
        String searchNorm = searchText.toLowerCase().replaceAll("\\s|\\.|-", "");

        var dadoJoin = root.join("dadoPessoal");

        // Normaliza CPF e RG no banco para comparação
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

        // Predicados de nome
        Predicate nameContains = criteriaBuilder.like(criteriaBuilder.lower(dadoJoin.get("nome")), search);
        Predicate nameStarts = criteriaBuilder.like(criteriaBuilder.lower(dadoJoin.get("nome")), searchPlain + "%");
        Predicate exactName = criteriaBuilder.equal(criteriaBuilder.lower(dadoJoin.get("nome")), searchPlain);

        // Predicados de CPF
        Predicate cpfContains = criteriaBuilder.like(cpfNormalizedDb, "%" + searchNorm + "%");
        Predicate cpfStarts = criteriaBuilder.like(cpfNormalizedDb, searchNorm + "%");
        Predicate exactCpf = criteriaBuilder.equal(cpfNormalizedDb, searchNorm);

        // Predicados de RG
        Predicate rgContains = criteriaBuilder.like(rgNormalizedDb, "%" + searchNorm + "%");
        Predicate rgStarts = criteriaBuilder.like(rgNormalizedDb, searchNorm + "%");
        Predicate exactRg = criteriaBuilder.equal(rgNormalizedDb, searchNorm);

        // Combinação geral
        Predicate anyContains = criteriaBuilder.or(nameContains, cpfContains, rgContains);
        predicate = criteriaBuilder.and(predicate, anyContains);

        // Ordenação por relevância
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

    // Buscar todos com a specification
    List<Acompanhante> allAcompanhantes = acompanhanteRepository.findAll(spec);

    int start = Math.min(offset, allAcompanhantes.size());
    int end = Math.min(offset + limit, allAcompanhantes.size());

    List<AcompanhanteDTO> nodes = allAcompanhantes.subList(start, end)
      .stream()
      .map(acompanhanteMapper::mapToDTO)
      .toList();

    boolean hasPreviousPage = offset > 0;
    boolean hasNextPage = (offset + limit) < allAcompanhantes.size();

    return paginatedMapper.toDTO(nodes, allAcompanhantes.size(), hasPreviousPage, hasNextPage);
  }

}
