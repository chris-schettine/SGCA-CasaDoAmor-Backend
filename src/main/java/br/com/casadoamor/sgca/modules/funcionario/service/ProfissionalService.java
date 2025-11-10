package br.com.casadoamor.sgca.modules.funcionario.service;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.common.repository.EnderecoRepository;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalRequestDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResponseDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResumoDTO;
import br.com.casadoamor.sgca.modules.funcionario.entity.Profissional;
import br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional;
import br.com.casadoamor.sgca.modules.funcionario.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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

        // Buscar endereço se informado
        Endereco endereco = null;
        if (dto.getEnderecoId() != null) {
            endereco = enderecoRepository.findById(dto.getEnderecoId())
                    .orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado"));
        }

        // Criar entidade
        Profissional profissional = Profissional.builder()
                .nome(dto.getNome())
                .cpfCriptografado(cpfHash)
                .telefone(dto.getTelefone())
                .email(dto.getEmail())
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

        // Atualizar campos
        profissional.setNome(dto.getNome());
        profissional.setTelefone(dto.getTelefone());
        profissional.setEmail(dto.getEmail());
        profissional.setAreaAtuacao(dto.getAreaAtuacao());
        profissional.setEspecialidade(dto.getEspecialidade());
        profissional.setNumeroRegistro(dto.getNumeroRegistro());
        profissional.setUfRegistro(dto.getUfRegistro());
        profissional.setDataDesligamento(dto.getDataDesligamento());
        profissional.setTipoContrato(dto.getTipoContrato());
        profissional.setCargaHoraria(dto.getCargaHoraria());
        profissional.setCargo(dto.getCargo());
        profissional.setDepartamento(dto.getDepartamento());
        profissional.setDisponibilidade(dto.getDisponibilidade());
        profissional.setObservacoes(dto.getObservacoes());
        profissional.setUpdatedBy(usuarioLogado);

        if (dto.getEnderecoId() != null) {
            Endereco endereco = enderecoRepository.findById(dto.getEnderecoId())
                    .orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado"));
            profissional.setEndereco(endereco);
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
    public Page<ProfissionalResumoDTO> listarComPaginacao(Pageable pageable) {
        return profissionalRepository.findAll(pageable)
                .map(this::toResumoDTO);
    }

    @Transactional
    public void inativar(String uuid, AuthUsuario usuarioLogado) {
        log.info("Inativando profissional: {}", uuid);
        
        Profissional profissional = profissionalRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));
        
        profissional.setAtivo(false);
        profissional.setUpdatedBy(usuarioLogado);
        profissionalRepository.save(profissional);
        
        log.info("Profissional inativado com sucesso: UUID={}", uuid);
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
        return ProfissionalResponseDTO.builder()
                .uuid(profissional.getUuid())
                .nome(profissional.getNome())
                .telefone(profissional.getTelefone())
                .email(profissional.getEmail())
                .categoria(profissional.getCategoria())
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
                .enderecoId(profissional.getEndereco() != null ? profissional.getEndereco().getId() : null)
                .ativo(profissional.getAtivo())
                .observacoes(profissional.getObservacoes())
                .createdAt(profissional.getCreatedAt())
                .createdByNome(profissional.getCreatedBy() != null ? profissional.getCreatedBy().getNome() : null)
                .updatedAt(profissional.getUpdatedAt())
                .updatedByNome(profissional.getUpdatedBy() != null ? profissional.getUpdatedBy().getNome() : null)
                .build();
    }

    private ProfissionalResumoDTO toResumoDTO(Profissional profissional) {
        return ProfissionalResumoDTO.builder()
                .uuid(profissional.getUuid())
                .nome(profissional.getNome())
                .telefone(profissional.getTelefone())
                .email(profissional.getEmail())
                .categoria(profissional.getCategoria())
                .areaAtuacao(profissional.getAreaAtuacao())
                .especialidade(profissional.getEspecialidade())
                .cargo(profissional.getCargo())
                .ativo(profissional.getAtivo())
                .build();
    }
}
