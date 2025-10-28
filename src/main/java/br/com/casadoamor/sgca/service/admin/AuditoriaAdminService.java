package br.com.casadoamor.sgca.service.admin;

import br.com.casadoamor.sgca.dto.admin.auditoria.AuditoriaPerfilDTO;
import br.com.casadoamor.sgca.dto.admin.auditoria.AuditoriaUsuarioDTO;
import br.com.casadoamor.sgca.entity.admin.Perfil;
import br.com.casadoamor.sgca.entity.admin.Permissao;
import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.repository.admin.PerfilRepository;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para operações de auditoria de usuários, perfis e permissões
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaAdminService {

    private final AuthUsuarioRepository authUsuarioRepository;
    private final PerfilRepository perfilRepository;

    /**
     * Busca informações completas de auditoria de um usuário
     */
    @Transactional(readOnly = true)
    public AuditoriaUsuarioDTO buscarAuditoriaUsuario(Long usuarioId) {
        log.info("Buscando auditoria para usuário ID: {}", usuarioId);

        AuthUsuario usuario = authUsuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Monta informação básica do usuário
        AuditoriaUsuarioDTO auditoria = AuditoriaUsuarioDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .cpf(usuario.getCpf())
                .tipo(usuario.getTipo().name())
                .ativo(usuario.getAtivo())
                .criadoEm(usuario.getCriadoEm())
                .atualizadoEm(usuario.getAtualizadoEm())
                .build();

        // Quem criou
        if (usuario.getCriadoPor() != null) {
            auditoria.setCriadoPor(mapToUsuarioResumo(usuario.getCriadoPor()));
        }

        // Quem atualizou
        if (usuario.getAtualizadoPor() != null) {
            auditoria.setAtualizadoPor(mapToUsuarioResumo(usuario.getAtualizadoPor()));
        }

        // Perfis
        if (usuario.getPerfis() != null && !usuario.getPerfis().isEmpty()) {
            List<AuditoriaUsuarioDTO.PerfilAuditoriaDTO> perfis = usuario.getPerfis().stream()
                    .map(perfil -> {
                        List<AuditoriaUsuarioDTO.PermissaoResumoDTO> permissoes = perfil.getPermissoes().stream()
                                .map(perm -> AuditoriaUsuarioDTO.PermissaoResumoDTO.builder()
                                        .id(perm.getId())
                                        .nome(perm.getNome())
                                        .descricao(perm.getDescricao())
                                        .build())
                                .collect(Collectors.toList());

                        return AuditoriaUsuarioDTO.PerfilAuditoriaDTO.builder()
                                .id(perfil.getId())
                                .nome(perfil.getNome())
                                .descricao(perfil.getDescricao())
                                .totalPermissoes(perfil.getPermissoes().size())
                                .permissoes(permissoes)
                                .build();
                    })
                    .collect(Collectors.toList());

            auditoria.setPerfis(perfis);
        }

        // Dados Pessoais
        if (usuario.getDadosPessoais() != null) {
            AuditoriaUsuarioDTO.DadosPessoaisAuditoriaDTO dadosPessoais = AuditoriaUsuarioDTO.DadosPessoaisAuditoriaDTO.builder()
                    .criadoEm(usuario.getDadosPessoais().getCriadoEm())
                    .atualizadoEm(usuario.getDadosPessoais().getAtualizadoEm())
                    .build();

            if (usuario.getDadosPessoais().getCriadoPor() != null) {
                dadosPessoais.setCriadoPor(mapToUsuarioResumo(usuario.getDadosPessoais().getCriadoPor()));
            }
            if (usuario.getDadosPessoais().getAtualizadoPor() != null) {
                dadosPessoais.setAtualizadoPor(mapToUsuarioResumo(usuario.getDadosPessoais().getAtualizadoPor()));
            }

            auditoria.setDadosPessoais(dadosPessoais);
        }

        // Endereço
        if (usuario.getEndereco() != null) {
            AuditoriaUsuarioDTO.EnderecoAuditoriaDTO endereco = AuditoriaUsuarioDTO.EnderecoAuditoriaDTO.builder()
                    .criadoEm(usuario.getEndereco().getCriadoEm())
                    .atualizadoEm(usuario.getEndereco().getAtualizadoEm())
                    .build();

            if (usuario.getEndereco().getCriadoPor() != null) {
                endereco.setCriadoPor(mapToUsuarioResumo(usuario.getEndereco().getCriadoPor()));
            }
            if (usuario.getEndereco().getAtualizadoPor() != null) {
                endereco.setAtualizadoPor(mapToUsuarioResumo(usuario.getEndereco().getAtualizadoPor()));
            }

            auditoria.setEndereco(endereco);
        }

        // Registro Profissional
        if (usuario.getRegistroProfissional() != null) {
            AuditoriaUsuarioDTO.RegistroProfissionalAuditoriaDTO registro = AuditoriaUsuarioDTO.RegistroProfissionalAuditoriaDTO.builder()
                    .tipoProfissional(usuario.getRegistroProfissional().getTipoProfissional().name())
                    .numeroRegistro(usuario.getRegistroProfissional().getNumeroRegistro())
                    .rqe(usuario.getRegistroProfissional().getRqe())
                    .criadoEm(usuario.getRegistroProfissional().getCriadoEm())
                    .build();

            if (usuario.getRegistroProfissional().getCriadoPor() != null) {
                registro.setCriadoPor(mapToUsuarioResumo(usuario.getRegistroProfissional().getCriadoPor()));
            }

            auditoria.setRegistroProfissional(registro);
        }

        log.info("Auditoria concluída para usuário ID: {}", usuarioId);
        return auditoria;
    }

    /**
     * Busca informações de auditoria de um perfil
     */
    @Transactional(readOnly = true)
    public AuditoriaPerfilDTO buscarAuditoriaPerfil(Long perfilId) {
        log.info("Buscando auditoria para perfil ID: {}", perfilId);

        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        AuditoriaPerfilDTO auditoria = AuditoriaPerfilDTO.builder()
                .id(perfil.getId())
                .nome(perfil.getNome())
                .descricao(perfil.getDescricao())
                .criadoEm(perfil.getCriadoEm())
                .atualizadoEm(perfil.getAtualizadoEm())
                .totalUsuarios(perfil.getUsuarios() != null ? perfil.getUsuarios().size() : 0)
                .build();

        // Quem criou o perfil
        if (perfil.getCriadoPor() != null) {
            AuthUsuario criador = authUsuarioRepository.findById(perfil.getCriadoPor())
                    .orElse(null);
            if (criador != null) {
                auditoria.setCriadoPor(mapToPerfilUsuarioResumo(criador));
            }
        }

        // Quem atualizou o perfil
        if (perfil.getAtualizadoPor() != null) {
            AuthUsuario atualizador = authUsuarioRepository.findById(perfil.getAtualizadoPor())
                    .orElse(null);
            if (atualizador != null) {
                auditoria.setAtualizadoPor(mapToPerfilUsuarioResumo(atualizador));
            }
        }

        // Permissões do perfil
        if (perfil.getPermissoes() != null && !perfil.getPermissoes().isEmpty()) {
            List<AuditoriaPerfilDTO.PermissaoComAuditoriaDTO> permissoes = perfil.getPermissoes().stream()
                    .map(this::mapToPermissaoComAuditoria)
                    .collect(Collectors.toList());
            auditoria.setPermissoes(permissoes);
        }

        // Usuários que possuem este perfil
        if (perfil.getUsuarios() != null && !perfil.getUsuarios().isEmpty()) {
            List<AuditoriaPerfilDTO.UsuarioResumoDTO> usuarios = perfil.getUsuarios().stream()
                    .limit(50) // Limita a 50 para não sobrecarregar a resposta
                    .map(this::mapToPerfilUsuarioResumo)
                    .collect(Collectors.toList());
            auditoria.setUsuarios(usuarios);
        }

        log.info("Auditoria concluída para perfil ID: {}", perfilId);
        return auditoria;
    }

    /**
     * Lista auditoria de todos os perfis
     */
    @Transactional(readOnly = true)
    public List<AuditoriaPerfilDTO> listarAuditoriaPerfis() {
        log.info("Listando auditoria de todos os perfis");

        return perfilRepository.findAll().stream()
                .map(perfil -> {
                    AuditoriaPerfilDTO dto = AuditoriaPerfilDTO.builder()
                            .id(perfil.getId())
                            .nome(perfil.getNome())
                            .descricao(perfil.getDescricao())
                            .criadoEm(perfil.getCriadoEm())
                            .atualizadoEm(perfil.getAtualizadoEm())
                            .totalUsuarios(perfil.getUsuarios() != null ? perfil.getUsuarios().size() : 0)
                            .build();

                    // Quem criou
                    if (perfil.getCriadoPor() != null) {
                        authUsuarioRepository.findById(perfil.getCriadoPor())
                                .ifPresent(criador -> dto.setCriadoPor(mapToPerfilUsuarioResumo(criador)));
                    }

                    // Quem atualizou
                    if (perfil.getAtualizadoPor() != null) {
                        authUsuarioRepository.findById(perfil.getAtualizadoPor())
                                .ifPresent(atualizador -> dto.setAtualizadoPor(mapToPerfilUsuarioResumo(atualizador)));
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Mapeia AuthUsuario para UsuarioResumoDTO
     */
    private AuditoriaUsuarioDTO.UsuarioResumoDTO mapToUsuarioResumo(AuthUsuario usuario) {
        return AuditoriaUsuarioDTO.UsuarioResumoDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .tipo(usuario.getTipo().name())
                .build();
    }

    /**
     * Mapeia AuthUsuario para UsuarioResumoDTO do Perfil
     */
    private AuditoriaPerfilDTO.UsuarioResumoDTO mapToPerfilUsuarioResumo(AuthUsuario usuario) {
        return AuditoriaPerfilDTO.UsuarioResumoDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .tipo(usuario.getTipo().name())
                .build();
    }

    /**
     * Mapeia Permissao para PermissaoComAuditoriaDTO
     */
    private AuditoriaPerfilDTO.PermissaoComAuditoriaDTO mapToPermissaoComAuditoria(Permissao permissao) {
        AuditoriaPerfilDTO.PermissaoComAuditoriaDTO dto = AuditoriaPerfilDTO.PermissaoComAuditoriaDTO.builder()
                .id(permissao.getId())
                .nome(permissao.getNome())
                .descricao(permissao.getDescricao())
                .criadoEm(permissao.getCriadoEm())
                .build();

        if (permissao.getCriadoPor() != null) {
            authUsuarioRepository.findById(permissao.getCriadoPor())
                    .ifPresent(criador -> dto.setCriadoPor(mapToPerfilUsuarioResumo(criador)));
        }

        return dto;
    }
}
