package br.com.casadoamor.sgca.service.admin;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.casadoamor.sgca.dto.admin.perfil.PerfilDTO;
import br.com.casadoamor.sgca.dto.admin.permissao.PermissaoDTO;
import br.com.casadoamor.sgca.dto.admin.user.AtribuirRolesDTO;
import br.com.casadoamor.sgca.dto.admin.user.CreateUserDTO;
import br.com.casadoamor.sgca.dto.admin.user.UpdateUserDTO;
import br.com.casadoamor.sgca.dto.admin.user.UserResponseDTO;
import br.com.casadoamor.sgca.entity.admin.Perfil;
import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.repository.admin.PerfilRepository;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;
import br.com.casadoamor.sgca.service.auth.AccountActivationService;
import lombok.RequiredArgsConstructor;

/**
 * Service para gerenciamento de usuários por administradores
 */
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final AuthUsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessaoService sessaoService;
    private final AccountActivationService accountActivationService;

    private static final String SAFE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$%";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Cria um novo usuário (admin)
     */
    @Transactional
    public UserResponseDTO criarUsuario(CreateUserDTO dto, Long adminId) {
        // Validações
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }
        if (usuarioRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new RuntimeException("CPF já cadastrado");
        }

        // Gera senha temporária
        String senhaTemporaria = gerarSenhaAleatoria();

        // Cria o usuário
        AuthUsuario usuario = AuthUsuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .cpf(dto.getCpf())
                .telefone(dto.getTelefone())
                .senhaHash(passwordEncoder.encode(senhaTemporaria))
                .ativo(true)
                .emailVerificado(false)
                .senhaTemporaria(true) // Marca como senha temporária
                .tentativasFalhasDeLogin(0)
                .build();

        // Define tipo
        try {
            usuario.setTipo(AuthUsuario.TipoUsuario.valueOf(dto.getTipo().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de usuário inválido: " + dto.getTipo());
        }

        // Busca admin que está criando
        AuthUsuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin não encontrado"));
        usuario.setCriadoPor(admin);

        // Salva usuário
        AuthUsuario salvo = usuarioRepository.save(usuario);

        // Atribui perfis se fornecidos
        if (dto.getPerfisIds() != null && !dto.getPerfisIds().isEmpty()) {
            atribuirPerfisInterno(salvo, dto.getPerfisIds(), adminId);
        }

        // Envia email de ativação com link + senha temporária
        try {
            accountActivationService.enviarEmailAtivacao(salvo, senhaTemporaria);
        } catch (Exception e) {
            // Log do erro mas não falha a criação
            System.err.println("Erro ao enviar email de ativação: " + e.getMessage());
        }

        return toDTO(salvo);
    }

    /**
     * Lista usuários com paginação
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(this::toDTO);
    }

    /**
     * Busca usuário por ID
     */
    @Transactional(readOnly = true)
    public UserResponseDTO buscarPorId(Long id) {
        AuthUsuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return toDTO(usuario);
    }

    /**
     * Atualiza dados do usuário
     */
    @Transactional
    public UserResponseDTO atualizarUsuario(Long id, UpdateUserDTO dto, Long adminId) {
        AuthUsuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Atualiza campos se fornecidos
        if (dto.getNome() != null) {
            usuario.setNome(dto.getNome());
        }
        if (dto.getTelefone() != null) {
            usuario.setTelefone(dto.getTelefone());
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(usuario.getEmail())) {
            // Valida se novo email já existe
            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Email já cadastrado");
            }
            usuario.setEmail(dto.getEmail());
            usuario.setEmailVerificado(false); // Precisa reverificar
        }
        if (dto.getAtivo() != null) {
            // Impede admin desativar a si mesmo
            if (id.equals(adminId) && !dto.getAtivo()) {
                throw new RuntimeException("Você não pode desativar sua própria conta");
            }
            usuario.setAtivo(dto.getAtivo());
        }
        if (dto.getTipo() != null) {
            try {
                usuario.setTipo(AuthUsuario.TipoUsuario.valueOf(dto.getTipo().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Tipo de usuário inválido: " + dto.getTipo());
            }
        }

        // Busca admin que está atualizando
        AuthUsuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin não encontrado"));
        usuario.setAtualizadoPor(admin);

        AuthUsuario atualizado = usuarioRepository.save(usuario);
        return toDTO(atualizado);
    }

    /**
     * Deleta usuário (soft delete)
     */
    @Transactional
    public void deletarUsuario(Long id, Long adminId) {
        // Impede admin deletar a si mesmo
        if (id.equals(adminId)) {
            throw new RuntimeException("Você não pode deletar sua própria conta");
        }

        AuthUsuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getDeletadoEm() != null) {
            throw new RuntimeException("Usuário já foi deletado");
        }

        usuario.setDeletadoEm(LocalDateTime.now());
        usuario.setAtivo(false);
        
        // Revoga todas sessões
        sessaoService.revogarTodasSessoes(id);
        
        usuarioRepository.save(usuario);
    }

    /**
     * Atribui perfis a um usuário
     */
    @Transactional
    public UserResponseDTO atribuirPerfis(Long usuarioId, AtribuirRolesDTO dto, Long adminId) {
        AuthUsuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        atribuirPerfisInterno(usuario, dto.getPerfisIds(), adminId);
        
        return toDTO(usuario);
    }

    /**
     * Force logout - revoga todas sessões do usuário
     */
    @Transactional
    public void forceLogout(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RuntimeException("Usuário não encontrado");
        }
        
        sessaoService.revogarTodasSessoes(usuarioId);
    }

    /**
     * Método interno para atribuir perfis
     */
    private void atribuirPerfisInterno(AuthUsuario usuario, List<Long> perfisIds, Long adminId) {
        // Remove perfis antigos
        usuario.getPerfis().clear();

        // Busca e adiciona novos perfis
        List<Perfil> perfis = perfilRepository.findByIdIn(perfisIds);
        if (perfis.size() != perfisIds.size()) {
            throw new RuntimeException("Alguns perfis não foram encontrados");
        }

        perfis.forEach(perfil -> {
            if (perfil.isDeletado()) {
                throw new RuntimeException("Perfil " + perfil.getNome() + " foi deletado");
            }
            usuario.getPerfis().add(perfil);
        });

        usuarioRepository.save(usuario);
    }

    /**
     * Gera senha aleatória segura
     */
    private String gerarSenhaAleatoria() {
        StringBuilder senha = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            senha.append(SAFE_CHARS.charAt(RANDOM.nextInt(SAFE_CHARS.length())));
        }
        return senha.toString();
    }

    /**
     * Converte entidade para DTO
     */
    private UserResponseDTO toDTO(AuthUsuario usuario) {
        List<PerfilDTO> perfisDTO = usuario.getPerfis().stream()
                .filter(p -> !p.isDeletado())
                .map(p -> PerfilDTO.builder()
                        .id(p.getId())
                        .nome(p.getNome())
                        .descricao(p.getDescricao())
                        .totalPermissoes(p.getPermissoes().size())
                        .permissoes(p.getPermissoes().stream()
                                .map(perm -> PermissaoDTO.builder()
                                        .id(perm.getId())
                                        .nome(perm.getNome())
                                        .descricao(perm.getDescricao())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return UserResponseDTO.builder()
                .id(usuario.getId())
                .uuid(usuario.getUuid())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .cpf(usuario.getCpf())
                .telefone(usuario.getTelefone())
                .tipo(usuario.getTipo().name())
                .ativo(usuario.getAtivo())
                .emailVerificado(usuario.getEmailVerificado())
                .ultimoLoginEm(usuario.getUltimoLoginEm())
                .criadoEm(usuario.getCriadoEm())
                .atualizadoEm(usuario.getAtualizadoEm())
                .perfis(perfisDTO)
                .build();
    }
}
