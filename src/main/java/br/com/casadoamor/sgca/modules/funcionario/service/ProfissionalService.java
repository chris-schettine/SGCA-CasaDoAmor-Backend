package br.com.casadoamor.sgca.modules.funcionario.service;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.common.repository.EnderecoRepository;
import br.com.casadoamor.sgca.modules.funcionario.dto.CategoriaProfissionalDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.DashboardStatsDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalRequestDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResponseDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResumoDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.TipoVinculoDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;
import br.com.casadoamor.sgca.modules.funcionario.entity.Profissional;
import br.com.casadoamor.sgca.modules.funcionario.entity.TipoVinculoEntity;
import br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional;
import br.com.casadoamor.sgca.modules.funcionario.repository.ProfissionalRepository;
import br.com.casadoamor.sgca.modules.funcionario.repository.TipoVinculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service para gerenciamento de profissionais (funcionários e voluntários)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final EnderecoRepository enderecoRepository;
    private final TipoVinculoRepository tipoVinculoRepository;

    @Transactional
    public ProfissionalResponseDTO cadastrar(ProfissionalRequestDTO dto, AuthUsuario usuarioLogado) {
        log.info("Cadastrando profissional: {} - Categoria: {}", dto.getNome(), dto.getCategoria());

        // Validar se CPF já existe
        byte[] cpfHash = hashCPF(dto.getCpf());
        if (profissionalRepository.findByCpfCriptografado(cpfHash).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        // Validar número de registro se informado
        if (dto.getNumeroRegistro() != null && dto.getUfRegistro() != null) {
            if (profissionalRepository.existsByNumeroRegistroAndUfRegistro(dto.getNumeroRegistro(), dto.getUfRegistro())) {
                throw new IllegalArgumentException("Número de registro profissional já cadastrado");
            }
        }

        // Buscar ou criar endereço
        Endereco endereco = null;
        if (dto.getEnderecoId() != null) {
            // Usar endereço existente
            endereco = enderecoRepository.findById(dto.getEnderecoId())
                    .orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado"));
        } else if (dto.getEndereco() != null) {
            // Criar novo endereço inline
            endereco = Endereco.builder()
                    .logradouro(dto.getEndereco().getLogradouro())
                    .numero(dto.getEndereco().getNumero())
                    .complemento(dto.getEndereco().getComplemento())
                    .bairro(dto.getEndereco().getBairro())
                    .cidade(dto.getEndereco().getCidade())
                    .estado(dto.getEndereco().getEstado())
                    .cep(dto.getEndereco().getCep())
                    .build();
            endereco = enderecoRepository.save(endereco);
            log.info("Endereço criado inline para profissional: ID={}", endereco.getId());
        }

        // Buscar tipo de vínculo
        TipoVinculoEntity tipoVinculo = null;
        if (dto.getTipoVinculoId() != null) {
            tipoVinculo = tipoVinculoRepository.findById(dto.getTipoVinculoId())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de vínculo não encontrado"));
        }

        // Criar entidade
        Profissional profissional = Profissional.builder()
                .nome(dto.getNome())
                .cpf(dto.getCpf())
                .cpfCriptografado(cpfHash)
                .telefone(dto.getTelefone())
                .email(dto.getEmail())
                .tipoVinculo(tipoVinculo)
                .categoria(dto.getCategoria())
                .areaAtuacao(dto.getAreaAtuacao())
                .especialidade(dto.getEspecialidade())
                .numeroRegistro(dto.getNumeroRegistro())
                .ufRegistro(dto.getUfRegistro())
                .dataAdmissao(dto.getDataAdmissao())
                .dataDesligamento(dto.getDataDesligamento())
                .tipoContrato(dto.getTipoContrato())
                .cargaHoraria(dto.getCargaHoraria())
                .cargo(dto.getCargo())
                .departamento(dto.getDepartamento())
                .disponibilidade(dto.getDisponibilidade())
                .endereco(endereco)
                .ativo(dto.getAtivo() != null ? dto.getAtivo() : true)
                .observacoes(dto.getObservacoes())
                .createdBy(usuarioLogado)
                .build();

        profissional = profissionalRepository.save(profissional);
        log.info("Profissional cadastrado com sucesso: UUID={}", profissional.getUuid());

        return toResponseDTO(profissional);
    }

    @Transactional
    public ProfissionalResponseDTO atualizar(String uuid, ProfissionalRequestDTO dto, AuthUsuario usuarioLogado) {
        log.info("Atualizando profissional: {}", uuid);

        Profissional profissional = profissionalRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

        // Atualizar apenas campos não nulos
        if (dto.getNome() != null) profissional.setNome(dto.getNome());
        if (dto.getTelefone() != null) profissional.setTelefone(dto.getTelefone());
        if (dto.getEmail() != null) profissional.setEmail(dto.getEmail());
        if (dto.getAreaAtuacao() != null) profissional.setAreaAtuacao(dto.getAreaAtuacao());
        if (dto.getEspecialidade() != null) profissional.setEspecialidade(dto.getEspecialidade());
        if (dto.getNumeroRegistro() != null) profissional.setNumeroRegistro(dto.getNumeroRegistro());
        if (dto.getUfRegistro() != null) profissional.setUfRegistro(dto.getUfRegistro());
        if (dto.getDataDesligamento() != null) profissional.setDataDesligamento(dto.getDataDesligamento());
        if (dto.getTipoContrato() != null) profissional.setTipoContrato(dto.getTipoContrato());
        if (dto.getCargaHoraria() != null) profissional.setCargaHoraria(dto.getCargaHoraria());
        if (dto.getCargo() != null) profissional.setCargo(dto.getCargo());
        if (dto.getDepartamento() != null) profissional.setDepartamento(dto.getDepartamento());
        if (dto.getDisponibilidade() != null) profissional.setDisponibilidade(dto.getDisponibilidade());
        if (dto.getObservacoes() != null) profissional.setObservacoes(dto.getObservacoes());
        profissional.setUpdatedBy(usuarioLogado);

        // Atualizar endereço
        if (dto.getEnderecoId() != null) {
            // Usar endereço existente
            Endereco endereco = enderecoRepository.findById(dto.getEnderecoId())
                    .orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado"));
            profissional.setEndereco(endereco);
        } else if (dto.getEndereco() != null) {
            // Criar ou atualizar endereço inline
            Endereco endereco = profissional.getEndereco();
            if (endereco == null) {
                endereco = new Endereco();
            }
            endereco.setLogradouro(dto.getEndereco().getLogradouro());
            endereco.setNumero(dto.getEndereco().getNumero());
            endereco.setComplemento(dto.getEndereco().getComplemento());
            endereco.setBairro(dto.getEndereco().getBairro());
            endereco.setCidade(dto.getEndereco().getCidade());
            endereco.setEstado(dto.getEndereco().getEstado());
            endereco.setCep(dto.getEndereco().getCep());
            endereco = enderecoRepository.save(endereco);
            profissional.setEndereco(endereco);
            log.info("Endereço atualizado inline para profissional: ID={}", endereco.getId());
        }

        if (dto.getAtivo() != null) {
            profissional.setAtivo(dto.getAtivo());
        }

        profissional = profissionalRepository.save(profissional);
        log.info("Profissional atualizado com sucesso: UUID={}", profissional.getUuid());

        return toResponseDTO(profissional);
    }

    @Transactional(readOnly = true)
    public ProfissionalResponseDTO buscarPorUuid(String uuid) {
        Profissional profissional = profissionalRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));
        return toResponseDTO(profissional);
    }

    @Transactional(readOnly = true)
    public List<ProfissionalResumoDTO> listarTodos() {
        return profissionalRepository.findAll().stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProfissionalResumoDTO> listarPorCategoria(CategoriaProfissional categoria) {
        return profissionalRepository.findByCategoria(categoria).stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProfissionalResumoDTO> listarAtivos() {
        return profissionalRepository.findByAtivoTrue().stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProfissionalResumoDTO> buscarPorNome(String nome) {
        return profissionalRepository.searchByNome(nome).stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProfissionalResumoDTO> buscarPorMultiplosCampos(String termo) {
        return profissionalRepository.searchByMultipleFields(termo).stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProfissionalResumoDTO> buscarAtivosPorMultiplosCampos(String termo) {
        return profissionalRepository.searchActiveByMultipleFields(termo).stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProfissionalResumoDTO> listarComPaginacao(Pageable pageable) {
        return profissionalRepository.findAll(pageable)
                .map(this::toResumoDTO);
    }

    @Transactional
    public void toggleStatus(String uuid, AuthUsuario usuarioLogado) {
        log.info("Alternando status do profissional: {}", uuid);
        
        Profissional profissional = profissionalRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));
        
        boolean novoStatus = !profissional.getAtivo();
        profissional.setAtivo(novoStatus);
        profissional.setUpdatedBy(usuarioLogado);
        profissionalRepository.save(profissional);
        
        log.info("Status do profissional alterado com sucesso: UUID={}, Novo Status={}", uuid, novoStatus ? "ATIVO" : "INATIVO");
    }

    @Transactional
    public void deletar(String uuid) {
        log.info("Deletando profissional (soft delete): {}", uuid);
        
        Profissional profissional = profissionalRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));
        
        profissionalRepository.delete(profissional);
        log.info("Profissional deletado com sucesso: UUID={}", uuid);
    }

    // Métodos auxiliares

    private byte[] hashCPF(String cpf) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(cpf.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao processar CPF", e);
        }
    }

    private ProfissionalResponseDTO toResponseDTO(Profissional profissional) {
        TipoVinculoDTO tipoVinculoDTO = null;
        if (profissional.getTipoVinculo() != null) {
            tipoVinculoDTO = TipoVinculoDTO.builder()
                .id(profissional.getTipoVinculo().getId())
                .codigo(profissional.getTipoVinculo().getCodigo())
                .nome(profissional.getTipoVinculo().getNome())
                .ativo(profissional.getTipoVinculo().getAtivo())
                .build();
        }
        
        // Map endereco
        EnderecoDTO enderecoDTO = null;
        if (profissional.getEndereco() != null) {
            enderecoDTO = EnderecoDTO.builder()
                .id(profissional.getEndereco().getId())
                .logradouro(profissional.getEndereco().getLogradouro())
                .numero(profissional.getEndereco().getNumero())
                .complemento(profissional.getEndereco().getComplemento())
                .bairro(profissional.getEndereco().getBairro())
                .cidade(profissional.getEndereco().getCidade())
                .estado(profissional.getEndereco().getEstado())
                .cep(profissional.getEndereco().getCep())
                .build();
        }
        
        // Map createdBy user
        ProfissionalResponseDTO.UsuarioResumoDTO createdByDTO = null;
        if (profissional.getCreatedBy() != null) {
            createdByDTO = ProfissionalResponseDTO.UsuarioResumoDTO.builder()
                .uuid(profissional.getCreatedBy().getUuid())
                .nome(profissional.getCreatedBy().getNome())
                .email(profissional.getCreatedBy().getEmail())
                .tipo(profissional.getCreatedBy().getTipo() != null ? profissional.getCreatedBy().getTipo().name() : null)
                .build();
        }
        
        // Map updatedBy user
        ProfissionalResponseDTO.UsuarioResumoDTO updatedByDTO = null;
        if (profissional.getUpdatedBy() != null) {
            updatedByDTO = ProfissionalResponseDTO.UsuarioResumoDTO.builder()
                .uuid(profissional.getUpdatedBy().getUuid())
                .nome(profissional.getUpdatedBy().getNome())
                .email(profissional.getUpdatedBy().getEmail())
                .tipo(profissional.getUpdatedBy().getTipo() != null ? profissional.getUpdatedBy().getTipo().name() : null)
                .build();
        }
        
        return ProfissionalResponseDTO.builder()
                .uuid(profissional.getUuid())
                .nome(profissional.getNome())
                .cpf(profissional.getCpf())
                .telefone(profissional.getTelefone())
                .email(profissional.getEmail())
                .tipoVinculo(tipoVinculoDTO)
                .categoria(new CategoriaProfissionalDTO(profissional.getCategoria().name(), profissional.getCategoria().getDescricao()))
                .areaAtuacao(profissional.getAreaAtuacao())
                .especialidade(profissional.getEspecialidade())
                .numeroRegistro(profissional.getNumeroRegistro())
                .ufRegistro(profissional.getUfRegistro())
                .dataAdmissao(profissional.getDataAdmissao())
                .dataDesligamento(profissional.getDataDesligamento())
                .tipoContrato(profissional.getTipoContrato())
                .cargaHoraria(profissional.getCargaHoraria())
                .cargo(profissional.getCargo())
                .departamento(profissional.getDepartamento())
                .disponibilidade(profissional.getDisponibilidade())
                .endereco(enderecoDTO)
                .ativo(profissional.getAtivo())
                .observacoes(profissional.getObservacoes())
                .fotoUrl(profissional.getFotoUrl())
                .fotoPath(profissional.getFotoPath())
                .fotoAtualizadaEm(profissional.getFotoAtualizadaEm())
                .createdAt(profissional.getCreatedAt())
                .createdBy(createdByDTO)
                .updatedAt(profissional.getUpdatedAt())
                .updatedBy(updatedByDTO)
                .build();
    }

    private ProfissionalResumoDTO toResumoDTO(Profissional profissional) {
        return ProfissionalResumoDTO.builder()
                .uuid(profissional.getUuid())
                .nome(profissional.getNome())
                .cpf(profissional.getCpf())
                .telefone(profissional.getTelefone())
                .email(profissional.getEmail())
                .numeroRegistro(profissional.getNumeroRegistro())
                .ufRegistro(profissional.getUfRegistro())
                .categoria(new CategoriaProfissionalDTO(profissional.getCategoria().name(), profissional.getCategoria().getDescricao()))
                .areaAtuacao(profissional.getAreaAtuacao())
                .especialidade(profissional.getEspecialidade())
                .cargo(profissional.getCargo())
                .ativo(profissional.getAtivo())
                .fotoUrl(profissional.getFotoUrl())
                .build();
    }
    
    /**
     * Obter estatísticas para o dashboard
     */
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        log.info("Obtendo estatísticas do dashboard");
        
        // Total de profissionais ativos e inativos
        Long totalAtivos = profissionalRepository.countByAtivo(true);
        Long totalInativos = profissionalRepository.countByAtivo(false);
        
        // Distribuição por categoria
        List<DashboardStatsDTO.CategoriaCount> porCategoria = profissionalRepository
                .findAll()
                .stream()
                .filter(Profissional::getAtivo)
                .collect(Collectors.groupingBy(Profissional::getCategoria, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> DashboardStatsDTO.CategoriaCount.builder()
                        .categoria(entry.getKey().name())
                        .label(getCategoriaLabel(entry.getKey()))
                        .count(entry.getValue())
                        .build())
                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
                .collect(Collectors.toList());
        
        // Distribuição por tipo de vínculo
        List<DashboardStatsDTO.TipoVinculoCount> porTipoVinculo = profissionalRepository
                .findAll()
                .stream()
                .filter(Profissional::getAtivo)
                .filter(p -> p.getTipoVinculo() != null)
                .collect(Collectors.groupingBy(Profissional::getTipoVinculo, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> DashboardStatsDTO.TipoVinculoCount.builder()
                        .codigo(entry.getKey().getCodigo())
                        .nome(entry.getKey().getNome())
                        .count(entry.getValue())
                        .build())
                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
                .collect(Collectors.toList());
        
        // Profissionais admitidos nos últimos 30 dias
        LocalDate dataLimite = LocalDate.now().minusDays(30);
        Long admitidosUltimos30Dias = profissionalRepository.findAll()
                .stream()
                .filter(p -> p.getDataAdmissao() != null && p.getDataAdmissao().isAfter(dataLimite))
                .count();
        
        // Profissionais com/sem endereço
        Long comEndereco = profissionalRepository.findAll()
                .stream()
                .filter(Profissional::getAtivo)
                .filter(p -> p.getEndereco() != null)
                .count();
        Long semEndereco = totalAtivos - comEndereco;
        
        // Top 5 áreas de atuação
        Map<String, Long> topAreasAtuacao = profissionalRepository.findAll()
                .stream()
                .filter(Profissional::getAtivo)
                .filter(p -> p.getAreaAtuacao() != null && !p.getAreaAtuacao().isEmpty())
                .collect(Collectors.groupingBy(Profissional::getAreaAtuacao, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        HashMap::new
                ));
        
        return DashboardStatsDTO.builder()
                .totalProfissionaisAtivos(totalAtivos)
                .totalProfissionaisInativos(totalInativos)
                .porCategoria(porCategoria)
                .porTipoVinculo(porTipoVinculo)
                .admitidosUltimos30Dias(admitidosUltimos30Dias)
                .comEnderecoCadastrado(comEndereco)
                .semEnderecoCadastrado(semEndereco)
                .topAreasAtuacao(topAreasAtuacao)
                .build();
    }
    
    /**
     * Helper para obter label amigável da categoria
     */
    private String getCategoriaLabel(CategoriaProfissional categoria) {
        return switch (categoria) {
            case MEDICO -> "Médico";
            case ENFERMAGEM -> "Enfermagem";
            case ODONTOLOGIA -> "Odontologia";
            case PSICOLOGIA -> "Psicologia";
            case NUTRICAO -> "Nutrição";
            case FISIOTERAPIA -> "Fisioterapia";
            case ASSISTENCIA_SOCIAL -> "Assistência Social";
            case PEDAGOGIA -> "Pedagogia";
            case ADMINISTRATIVO -> "Administrativo";
            case RECEPCAO -> "Recepção";
            case OUTROS -> "Outros";
        };
    }
}
