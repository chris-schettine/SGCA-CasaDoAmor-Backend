package br.com.casadoamor.sgca.service.auth;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.casadoamor.sgca.dto.auth.request.ChangePasswordRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.ForgotPasswordRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.LoginRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.RegisterRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.ResetPasswordRequestDTO;
import br.com.casadoamor.sgca.dto.auth.request.VerifyEmailRequestDTO;
import br.com.casadoamor.sgca.dto.auth.response.AuthResponseDTO;
import br.com.casadoamor.sgca.dto.common.MessageResponseDTO;
import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.entity.auth.TokenRecuperacao;
import br.com.casadoamor.sgca.enums.TipoToken;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;
import br.com.casadoamor.sgca.security.JwtUtil;
import br.com.casadoamor.sgca.service.admin.AuditoriaService;
import br.com.casadoamor.sgca.service.admin.SessaoService;
import br.com.casadoamor.sgca.util.PasswordValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Serviço de autenticação - contém lógica de negócio para registro e login
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUsuarioRepository authUsuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuditoriaService auditoriaService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;
    private final SessaoService sessaoService;
    private final HistoricoSenhaService historicoSenhaService;

    /**
     * Registra um novo usuário no sistema
     */
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Valida política de senha
        PasswordValidator.validarOuLancarExcecao(request.getSenha());
        
        // Verifica se o email já existe
        Optional<AuthUsuario> existingUserByEmail = authUsuarioRepository.findByEmail(request.getEmail());
        if (existingUserByEmail.isPresent()) {
            throw new RuntimeException("Email já cadastrado no sistema");
        }

        // Verifica se o CPF já existe
        Optional<AuthUsuario> existingUserByCpf = authUsuarioRepository.findByCpf(request.getCpf());
        if (existingUserByCpf.isPresent()) {
            throw new RuntimeException("CPF já cadastrado no sistema");
        }

        // Cria o novo usuário
        AuthUsuario novoUsuario = AuthUsuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senhaHash(passwordEncoder.encode(request.getSenha())) // Hash da senha
                .cpf(request.getCpf())
                .telefone(request.getTelefone())
                .ativo(true)
                .emailVerificado(false) // Pode implementar verificação de email depois
                .tentativasFalhasDeLogin(0)
                .build();

        // Define o tipo de usuário (padrão: RECEPCIONISTA)
        if (request.getTipo() != null && !request.getTipo().isEmpty()) {
            try {
                novoUsuario.setTipo(AuthUsuario.TipoUsuario.valueOf(request.getTipo().toUpperCase()));
            } catch (IllegalArgumentException e) {
                novoUsuario.setTipo(AuthUsuario.TipoUsuario.RECEPCIONISTA);
            }
        } else {
            novoUsuario.setTipo(AuthUsuario.TipoUsuario.RECEPCIONISTA);
        }

        // Salva o usuário no banco de dados
        AuthUsuario usuarioSalvo = authUsuarioRepository.save(novoUsuario);

        // Salva a senha inicial no histórico
        historicoSenhaService.salvarHistorico(usuarioSalvo.getId(), usuarioSalvo.getSenhaHash());

        // Cria um UserDetails para gerar o token
        // IMPORTANTE: username agora é o CPF
        UserDetails userDetails = User.builder()
                .username(usuarioSalvo.getCpf()) // CPF como username
                .password(usuarioSalvo.getSenhaHash())
                .authorities("ROLE_" + usuarioSalvo.getTipo().name())
                .build();

        // Gera o token JWT
        String token = jwtUtil.generateToken(userDetails);

        // Retorna a resposta com o token
        return AuthResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .email(usuarioSalvo.getEmail())
                .nome(usuarioSalvo.getNome())
                .tipoUsuario(usuarioSalvo.getTipo().name())
                .expiresIn(jwtUtil.getExpirationTime())
                .build();
    }

    /**
     * Realiza o login do usuário usando CPF
     */
    public AuthResponseDTO login(LoginRequestDTO request, HttpServletRequest httpRequest) {
        String ipOrigem = obterIpOrigem(httpRequest);
        String userAgent = obterUserAgent(httpRequest);

        // Verifica se CPF está bloqueado por excesso de tentativas
        if (auditoriaService.verificarBloqueio(request.getCpf())) {
            auditoriaService.registrarLoginFalha(request.getCpf(), ipOrigem, userAgent, "CONTA_BLOQUEADA");
            throw new RuntimeException("Conta bloqueada temporariamente por excesso de tentativas");
        }

        // Busca o usuário pelo CPF
        AuthUsuario usuario = authUsuarioRepository.findByCpf(request.getCpf())
                .orElseThrow(() -> {
                    auditoriaService.registrarLoginFalha(request.getCpf(), ipOrigem, userAgent, "CPF_INVALIDO");
                    return new RuntimeException("Credenciais inválidas");
                });

        // Verifica se o usuário está ativo
        if (!usuario.getAtivo()) {
            auditoriaService.registrarLoginFalha(request.getCpf(), ipOrigem, userAgent, "CONTA_INATIVA");
            throw new RuntimeException("Conta inativa. Entre em contato com o administrador.");
        }

        // Verifica se a conta está bloqueada
        if (usuario.getLockedUntil() != null && usuario.getLockedUntil().isAfter(LocalDateTime.now())) {
            auditoriaService.registrarLoginFalha(request.getCpf(), ipOrigem, userAgent, "CONTA_BLOQUEADA");
            throw new RuntimeException("Conta bloqueada até: " + usuario.getLockedUntil());
        }

        // Verifica se tem senha temporária e email não verificado
        if (usuario.getSenhaTemporaria() && !usuario.getEmailVerificado()) {
            auditoriaService.registrarLoginFalha(request.getCpf(), ipOrigem, userAgent, "CONTA_NAO_ATIVADA");
            throw new RuntimeException("Você precisa ativar sua conta primeiro. Verifique seu email.");
        }

        try {
            // Autentica o usuário usando o AuthenticationManager do Spring Security
            // IMPORTANTE: CPF é usado como username
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getCpf(), // CPF como username
                            request.getSenha()
                    )
            );

            // Reseta as tentativas falhas de login
            if (usuario.getTentativasFalhasDeLogin() > 0) {
                usuario.setTentativasFalhasDeLogin(0);
                usuario.setLockedUntil(null);
                authUsuarioRepository.save(usuario);
            }

            // Atualiza o último login
            usuario.setUltimoLoginEm(LocalDateTime.now());
            authUsuarioRepository.save(usuario);

            // Gera o token JWT
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            // Registra tentativa de login bem-sucedida
            auditoriaService.registrarLoginSucesso(usuario, ipOrigem, userAgent);

            // Cria sessão JWT
            LocalDateTime expiracao = LocalDateTime.now().plusSeconds(jwtUtil.getExpirationTime() / 1000);
            sessaoService.criarSessao(usuario, token, ipOrigem, userAgent, expiracao);

            // Retorna a resposta com o token
            return AuthResponseDTO.builder()
                    .token(token)
                    .tipo("Bearer")
                    .email(usuario.getEmail())
                    .nome(usuario.getNome())
                    .tipoUsuario(usuario.getTipo().name())
                    .expiresIn(jwtUtil.getExpirationTime())
                    .build();

        } catch (Exception e) {
            // Incrementa as tentativas falhas de login
            usuario.setTentativasFalhasDeLogin(usuario.getTentativasFalhasDeLogin() + 1);

            // Bloqueia a conta após 5 tentativas falhas
            if (usuario.getTentativasFalhasDeLogin() >= 5) {
                usuario.setLockedUntil(LocalDateTime.now().plusMinutes(30)); // Bloqueia por 30 minutos
            }

            authUsuarioRepository.save(usuario);

            // Registra tentativa de login falhada
            auditoriaService.registrarLoginFalha(request.getCpf(), ipOrigem, userAgent, "SENHA_INVALIDA");

            throw new RuntimeException("Credenciais inválidas");
        }
    }

    /**
     * Solicita recuperação de senha
     */
    public MessageResponseDTO forgotPassword(ForgotPasswordRequestDTO request) {
        Optional<AuthUsuario> usuarioOpt = authUsuarioRepository.findByEmail(request.getEmail());

        // Por segurança, sempre retorna sucesso mesmo se email não existir
        if (usuarioOpt.isEmpty()) {
            return MessageResponseDTO.success("Se o email estiver cadastrado, você receberá instruções para recuperação");
        }

        AuthUsuario usuario = usuarioOpt.get();
        String token = recuperacaoSenhaService.gerarTokenRecuperacao(usuario);
        recuperacaoSenhaService.enviarEmailRecuperacao(usuario, token);

        return MessageResponseDTO.success("Se o email estiver cadastrado, você receberá instruções para recuperação");
    }

    /**
     * Redefine senha com token de recuperação
     */
    public MessageResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        // Valida política de senha
        PasswordValidator.validarOuLancarExcecao(request.getNovaSenha());
        
        TokenRecuperacao token = recuperacaoSenhaService
                .validarToken(request.getToken(), TipoToken.RECUPERACAO_SENHA)
                .orElseThrow(() -> new RuntimeException("Token inválido ou expirado"));

        AuthUsuario usuario = token.getUsuario();
        
        // Verifica se a senha já foi usada recentemente
        if (historicoSenhaService.senhaJaUsada(usuario.getId(), request.getNovaSenha())) {
            throw new IllegalArgumentException(
                "Esta senha já foi utilizada recentemente. Por favor, escolha uma senha diferente."
            );
        }
        
        String novaSenhaHash = passwordEncoder.encode(request.getNovaSenha());
        usuario.setSenhaHash(novaSenhaHash);
        usuario.setTentativasFalhasDeLogin(0);
        usuario.setLockedUntil(null);
        authUsuarioRepository.save(usuario);
        
        // Salva no histórico
        historicoSenhaService.salvarHistorico(usuario.getId(), novaSenhaHash);

        recuperacaoSenhaService.marcarTokenComoUsado(token);

        return MessageResponseDTO.success("Senha redefinida com sucesso");
    }

    /**
     * Verifica email do usuário
     */
    public MessageResponseDTO verifyEmail(VerifyEmailRequestDTO request) {
        TokenRecuperacao token = recuperacaoSenhaService
                .validarToken(request.getToken(), TipoToken.VERIFICACAO_EMAIL)
                .orElseThrow(() -> new RuntimeException("Token inválido ou expirado"));

        AuthUsuario usuario = token.getUsuario();
        usuario.setEmailVerificado(true);
        authUsuarioRepository.save(usuario);
        recuperacaoSenhaService.marcarTokenComoUsado(token);

        return MessageResponseDTO.success("Email verificado com sucesso");
    }

    /**
     * Altera senha do usuário autenticado
     * @param request dados da requisição
     * @param cpf CPF do usuário autenticado (extraído do token JWT)
     */
    public MessageResponseDTO changePassword(ChangePasswordRequestDTO request, String cpf) {
        // Valida política de senha
        PasswordValidator.validarOuLancarExcecao(request.getNovaSenha());
        
        AuthUsuario usuario = authUsuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verifica senha atual
        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenhaHash())) {
            throw new RuntimeException("Senha atual incorreta");
        }

        // Verifica se a senha já foi usada recentemente
        if (historicoSenhaService.senhaJaUsada(usuario.getId(), request.getNovaSenha())) {
            throw new IllegalArgumentException(
                "Esta senha já foi utilizada recentemente. Por favor, escolha uma senha diferente."
            );
        }

        // Atualiza senha
        String novaSenhaHash = passwordEncoder.encode(request.getNovaSenha());
        usuario.setSenhaHash(novaSenhaHash);
        authUsuarioRepository.save(usuario);
        
        // Salva no histórico
        historicoSenhaService.salvarHistorico(usuario.getId(), novaSenhaHash);

        return MessageResponseDTO.success("Senha alterada com sucesso");
    }

    /**
     * Busca o ID do usuário pelo CPF
     * @param cpf CPF do usuário
     * @return Optional com o ID do usuário
     */
    public Optional<Long> findUserIdByCpf(String cpf) {
        return authUsuarioRepository.findByCpf(cpf)
                .map(AuthUsuario::getId);
    }

    /**
     * Busca ID do usuário por CPF (lança exceção se não encontrar)
     */
    public Long buscarIdPorCpf(String cpf) {
        return authUsuarioRepository.findByCpf(cpf)
                .map(AuthUsuario::getId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    /**
     * Gera token JWT por CPF (usado no 2FA)
     */
    public AuthResponseDTO gerarTokenPorCpf(String cpf) {
        AuthUsuario usuario = authUsuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        UserDetails userDetails = User.builder()
                .username(usuario.getCpf())
                .password(usuario.getSenhaHash())
                .authorities("ROLE_" + usuario.getTipo().name())
                .build();

        String token = jwtUtil.generateToken(userDetails);

        return AuthResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .email(usuario.getEmail())
                .nome(usuario.getNome())
                .tipoUsuario(usuario.getTipo().name())
                .expiresIn(jwtUtil.getExpirationTime())
                .build();
    }

    /**
     * Extrai IP de origem da requisição
     */
    private String obterIpOrigem(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Extrai User-Agent da requisição
     */
    private String obterUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown";
    }
}
