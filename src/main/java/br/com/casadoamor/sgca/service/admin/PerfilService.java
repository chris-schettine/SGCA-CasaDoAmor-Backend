package br.com.casadoamor.sgca.service.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.casadoamor.sgca.dto.admin.perfil.CreatePerfilDTO;
import br.com.casadoamor.sgca.dto.admin.perfil.PerfilDTO;
import br.com.casadoamor.sgca.dto.admin.permissao.PermissaoDTO;
import br.com.casadoamor.sgca.entity.admin.Perfil;
import br.com.casadoamor.sgca.entity.admin.Permissao;
import br.com.casadoamor.sgca.repository.admin.PerfilRepository;
import br.com.casadoamor.sgca.repository.admin.PermissaoRepository;
import lombok.RequiredArgsConstructor;

/**
 * Service para gerenciamento de Perfis (Roles)
 */
@Service
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;

    /**
     * Cria um novo perfil
     */
    @Transactional
    public PerfilDTO criarPerfil(CreatePerfilDTO dto, Long criadoPor) {
        // Valida se nome já existe
        if (perfilRepository.existsByNome(dto.getNome())) {
            throw new RuntimeException("Perfil com nome '" + dto.getNome() + "' já existe");
        }

        // Cria o perfil
        Perfil perfil = Perfil.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .criadoPor(criadoPor)
                .build();

        // Adiciona permissões se fornecidas
        if (dto.getPermissoesIds() != null && !dto.getPermissoesIds().isEmpty()) {
            List<Permissao> permissoes = permissaoRepository.findByIdIn(dto.getPermissoesIds());
            permissoes.forEach(perfil::adicionarPermissao);
        }

        Perfil salvo = perfilRepository.save(perfil);
        return toDTO(salvo);
    }

    /**
     * Lista todos os perfis ativos
     */
    @Transactional(readOnly = true)
    public List<PerfilDTO> listarPerfis() {
        return perfilRepository.findAllAtivos().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca perfil por ID
     */
    @Transactional(readOnly = true)
    public PerfilDTO buscarPorId(Long id) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        
        if (perfil.isDeletado()) {
            throw new RuntimeException("Perfil foi deletado");
        }
        
        return toDTO(perfil);
    }

    /**
     * Atualiza um perfil
     */
    @Transactional
    public PerfilDTO atualizarPerfil(Long id, CreatePerfilDTO dto, Long atualizadoPor) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        if (perfil.isDeletado()) {
            throw new RuntimeException("Perfil foi deletado");
        }

        // Atualiza dados
        if (dto.getDescricao() != null) {
            perfil.setDescricao(dto.getDescricao());
        }

        // Atualiza permissões
        if (dto.getPermissoesIds() != null) {
            // Remove todas permissões antigas
            perfil.getPermissoes().clear();
            
            // Adiciona novas permissões
            List<Permissao> permissoes = permissaoRepository.findByIdIn(dto.getPermissoesIds());
            permissoes.forEach(perfil::adicionarPermissao);
        }

        perfil.setAtualizadoEm(LocalDateTime.now());
        perfil.setAtualizadoPor(atualizadoPor);

        Perfil atualizado = perfilRepository.save(perfil);
        return toDTO(atualizado);
    }

    /**
     * Deleta um perfil (soft delete)
     */
    @Transactional
    public void deletarPerfil(Long id) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        if (perfil.isDeletado()) {
            throw new RuntimeException("Perfil já foi deletado");
        }

        // Verifica se há usuários com este perfil
        if (!perfil.getUsuarios().isEmpty()) {
            throw new RuntimeException("Não é possível deletar perfil com usuários associados. Total: " + perfil.getUsuarios().size());
        }

        perfil.setDeletadoEm(LocalDateTime.now());
        perfilRepository.save(perfil);
    }

    /**
     * Adiciona permissões a um perfil (sem remover as existentes)
     */
    @Transactional
    public PerfilDTO adicionarPermissoes(Long perfilId, List<Long> permissoesIds, Long atualizadoPor) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        if (perfil.isDeletado()) {
            throw new RuntimeException("Perfil foi deletado");
        }

        // Busca permissões
        List<Permissao> permissoes = permissaoRepository.findByIdIn(permissoesIds);
        if (permissoes.size() != permissoesIds.size()) {
            throw new RuntimeException("Algumas permissões não foram encontradas");
        }

        // Adiciona permissões (evita duplicatas)
        permissoes.forEach(permissao -> {
            if (permissao.isDeletado()) {
                throw new RuntimeException("Permissão " + permissao.getNome() + " foi deletada");
            }
            perfil.adicionarPermissao(permissao);
        });

        perfil.setAtualizadoEm(LocalDateTime.now());
        perfil.setAtualizadoPor(atualizadoPor);

        Perfil atualizado = perfilRepository.save(perfil);
        return toDTO(atualizado);
    }

    /**
     * Remove permissões de um perfil
     */
    @Transactional
    public PerfilDTO removerPermissoes(Long perfilId, List<Long> permissoesIds, Long atualizadoPor) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        if (perfil.isDeletado()) {
            throw new RuntimeException("Perfil foi deletado");
        }

        // Busca permissões
        List<Permissao> permissoes = permissaoRepository.findByIdIn(permissoesIds);
        
        // Remove permissões
        permissoes.forEach(perfil::removerPermissao);

        perfil.setAtualizadoEm(LocalDateTime.now());
        perfil.setAtualizadoPor(atualizadoPor);

        Perfil atualizado = perfilRepository.save(perfil);
        return toDTO(atualizado);
    }

    /**
     * Converte entidade para DTO
     */
    private PerfilDTO toDTO(Perfil perfil) {
        List<PermissaoDTO> permissoesDTO = perfil.getPermissoes().stream()
                .filter(p -> !p.isDeletado())
                .map(p -> PermissaoDTO.builder()
                        .id(p.getId())
                        .nome(p.getNome())
                        .descricao(p.getDescricao())
                        .build())
                .collect(Collectors.toList());

        return PerfilDTO.builder()
                .id(perfil.getId())
                .nome(perfil.getNome())
                .descricao(perfil.getDescricao())
                .permissoes(permissoesDTO)
                .totalPermissoes(permissoesDTO.size())
                .build();
    }
}
