package br.com.casadoamor.sgca.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;

/**
 * Implementação do UserDetailsService do Spring Security
 * Carrega usuários do banco de dados para autenticação
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AuthUsuarioRepository authUsuarioRepository;

    /**
     * Carrega o usuário pelo CPF (username no contexto do Spring Security)
     */
    @Override
    public UserDetails loadUserByUsername(String cpf) throws UsernameNotFoundException {
        // Busca o usuário no banco de dados pelo CPF
        AuthUsuario usuario = authUsuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com CPF: " + cpf));

        // Verifica se o usuário está ativo
        if (!usuario.getAtivo()) {
            throw new UsernameNotFoundException("Usuário inativo: " + cpf);
        }

        // Verifica se a conta está bloqueada
        if (usuario.getLockedUntil() != null && usuario.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            throw new UsernameNotFoundException("Conta bloqueada até: " + usuario.getLockedUntil());
        }

        // Cria as autoridades (roles) do usuário
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getTipo().name()));

        // Retorna um UserDetails do Spring Security
        // IMPORTANTE: username agora é o CPF
        return new User(
                usuario.getCpf(), // CPF como username
                usuario.getSenhaHash(),
                usuario.getAtivo(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                usuario.getLockedUntil() == null || usuario.getLockedUntil().isBefore(java.time.LocalDateTime.now()), // accountNonLocked
                authorities
        );
    }
}
