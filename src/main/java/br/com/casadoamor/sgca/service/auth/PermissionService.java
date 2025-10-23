package br.com.casadoamor.sgca.service.auth;

import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço para verificação de permissões de usuários
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final AuthUsuarioRepository usuarioRepository;

    /**
     * Verifica se o usuário possui uma permissão específica
     */
    public boolean hasPermission(String cpf, String permissionName) {
        try {
            AuthUsuario usuario = usuarioRepository.findByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            return usuario.getPerfis().stream()
                    .flatMap(perfil -> perfil.getPermissoes().stream())
                    .anyMatch(permissao -> permissao.getNome().equals(permissionName));
        } catch (Exception e) {
            log.error("Erro ao verificar permissão {} para usuário {}: {}", permissionName, cpf, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário possui todas as permissões especificadas
     */
    public boolean hasAllPermissions(String cpf, String... permissionNames) {
        for (String permission : permissionNames) {
            if (!hasPermission(cpf, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica se o usuário possui ao menos uma das permissões especificadas
     */
    public boolean hasAnyPermission(String cpf, String... permissionNames) {
        for (String permission : permissionNames) {
            if (hasPermission(cpf, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtém todas as permissões do usuário
     */
    public Set<String> getUserPermissions(String cpf) {
        try {
            AuthUsuario usuario = usuarioRepository.findByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            return usuario.getPerfis().stream()
                    .flatMap(perfil -> perfil.getPermissoes().stream())
                    .map(permissao -> permissao.getNome())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Erro ao obter permissões do usuário {}: {}", cpf, e.getMessage());
            return Set.of();
        }
    }

    /**
     * Verifica se o usuário possui o tipo ADMINISTRADOR
     */
    public boolean isAdmin(String cpf) {
        try {
            AuthUsuario usuario = usuarioRepository.findByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            return "ADMINISTRADOR".equals(usuario.getTipo().name());
        } catch (Exception e) {
            log.error("Erro ao verificar se usuário {} é admin: {}", cpf, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica permissão baseada no Authentication do Spring Security
     */
    public boolean hasPermission(Authentication authentication, String permissionName) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String cpf = authentication.getName();
        return hasPermission(cpf, permissionName);
    }

    /**
     * Obtém authorities (permissões + roles) do usuário para o Spring Security
     */
    public Collection<? extends GrantedAuthority> getAuthorities(String cpf) {
        try {
            AuthUsuario usuario = usuarioRepository.findByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Set<GrantedAuthority> authorities = usuario.getPerfis().stream()
                    .flatMap(perfil -> perfil.getPermissoes().stream())
                    .map(permissao -> (GrantedAuthority) () -> permissao.getNome())
                    .collect(Collectors.toSet());

            // Adiciona o tipo de usuário como role
            authorities.add((GrantedAuthority) () -> "ROLE_" + usuario.getTipo().name());

            return authorities;
        } catch (Exception e) {
            log.error("Erro ao obter authorities do usuário {}: {}", cpf, e.getMessage());
            return Set.of();
        }
    }
}
