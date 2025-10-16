package br.com.casadoamor.sgca.service.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.casadoamor.sgca.dto.admin.permissao.CreatePermissaoDTO;
import br.com.casadoamor.sgca.dto.admin.permissao.PermissaoDTO;
import br.com.casadoamor.sgca.entity.admin.Permissao;
import br.com.casadoamor.sgca.repository.admin.PermissaoRepository;
import lombok.RequiredArgsConstructor;

/**
 * Service para gerenciamento de Permissões
 */
@Service
@RequiredArgsConstructor
public class PermissaoService {

    private final PermissaoRepository permissaoRepository;

    /**
     * Cria uma nova permissão
     */
    @Transactional
    public PermissaoDTO criarPermissao(CreatePermissaoDTO dto, Long criadoPor) {
        // Valida se nome já existe
        if (permissaoRepository.existsByNome(dto.getNome())) {
            throw new RuntimeException("Permissão com nome '" + dto.getNome() + "' já existe");
        }

        // Cria a permissão
        Permissao permissao = Permissao.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .criadoPor(criadoPor)
                .build();

        Permissao salva = permissaoRepository.save(permissao);
        return toDTO(salva);
    }

    /**
     * Lista todas as permissões ativas
     */
    @Transactional(readOnly = true)
    public List<PermissaoDTO> listarPermissoes() {
        return permissaoRepository.findAllAtivas().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca permissão por ID
     */
    @Transactional(readOnly = true)
    public PermissaoDTO buscarPorId(Long id) {
        Permissao permissao = permissaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permissão não encontrada"));
        
        if (permissao.isDeletado()) {
            throw new RuntimeException("Permissão foi deletada");
        }
        
        return toDTO(permissao);
    }

    /**
     * Atualiza uma permissão
     */
    @Transactional
    public PermissaoDTO atualizarPermissao(Long id, CreatePermissaoDTO dto, Long atualizadoPor) {
        Permissao permissao = permissaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permissão não encontrada"));

        if (permissao.isDeletado()) {
            throw new RuntimeException("Permissão foi deletada");
        }

        // Atualiza dados
        if (dto.getDescricao() != null) {
            permissao.setDescricao(dto.getDescricao());
        }

        permissao.setAtualizadoEm(LocalDateTime.now());
        permissao.setAtualizadoPor(atualizadoPor);

        Permissao atualizada = permissaoRepository.save(permissao);
        return toDTO(atualizada);
    }

    /**
     * Deleta uma permissão (soft delete)
     */
    @Transactional
    public void deletarPermissao(Long id) {
        Permissao permissao = permissaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permissão não encontrada"));

        if (permissao.isDeletado()) {
            throw new RuntimeException("Permissão já foi deletada");
        }

        // Verifica se há perfis com esta permissão
        if (!permissao.getPerfis().isEmpty()) {
            throw new RuntimeException("Não é possível deletar permissão associada a perfis. Total: " + permissao.getPerfis().size());
        }

        permissao.setDeletadoEm(LocalDateTime.now());
        permissaoRepository.save(permissao);
    }

    /**
     * Converte entidade para DTO
     */
    private PermissaoDTO toDTO(Permissao permissao) {
        return PermissaoDTO.builder()
                .id(permissao.getId())
                .nome(permissao.getNome())
                .descricao(permissao.getDescricao())
                .build();
    }
}
