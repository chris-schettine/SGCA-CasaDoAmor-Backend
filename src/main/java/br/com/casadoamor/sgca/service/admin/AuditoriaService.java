package br.com.casadoamor.sgca.service.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.entity.auth.TentativaLogin;
import br.com.casadoamor.sgca.repository.auth.TentativaLoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para auditoria de tentativas de login
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final TentativaLoginRepository tentativaLoginRepository;

    private static final int MAX_TENTATIVAS = 5;
    private static final int MINUTOS_BLOQUEIO = 30;

    /**
     * Registra tentativa de login bem-sucedida
     */
    @Transactional
    public void registrarLoginSucesso(AuthUsuario usuario, String ipOrigem, String userAgent) {
        TentativaLogin tentativa = TentativaLogin.builder()
                .usuario(usuario)
                .cpf(usuario.getCpf())
                .ipOrigem(ipOrigem)
                .userAgent(userAgent)
                .sucesso(true)
                .bloqueado(false)
                .build();

        tentativaLoginRepository.save(tentativa);
        log.info("Login bem-sucedido registrado para usuário: {}", usuario.getEmail());
    }

    /**
     * Registra tentativa de login com falha
     */
    @Transactional
    public void registrarLoginFalha(String cpf, String ipOrigem, String userAgent, String motivoFalha) {
        TentativaLogin tentativa = TentativaLogin.builder()
                .cpf(cpf)
                .ipOrigem(ipOrigem)
                .userAgent(userAgent)
                .sucesso(false)
                .motivoFalha(motivoFalha)
                .bloqueado(verificarBloqueio(cpf))
                .build();

        tentativaLoginRepository.save(tentativa);
        log.warn("Login falho registrado para CPF: {} - Motivo: {}", cpf, motivoFalha);
    }

    /**
     * Verifica se CPF está bloqueado por excesso de tentativas
     */
    public boolean verificarBloqueio(String cpf) {
        LocalDateTime limiteData = LocalDateTime.now().minusMinutes(MINUTOS_BLOQUEIO);
        
        Long tentativasFalhas = tentativaLoginRepository
                .countByCpfAndSucessoAndDataTentativaAfter(cpf, false, limiteData);

        boolean bloqueado = tentativasFalhas >= MAX_TENTATIVAS;
        
        if (bloqueado) {
            log.warn("CPF {} bloqueado por excesso de tentativas ({})", cpf, tentativasFalhas);
        }
        
        return bloqueado;
    }

    /**
     * Obtém histórico de tentativas de login de um usuário
     */
    @Transactional(readOnly = true)
    public List<TentativaLogin> obterHistoricoUsuario(Long usuarioId) {
        return tentativaLoginRepository.findByUsuarioIdOrderByDataTentativaDesc(usuarioId);
    }

    /**
     * Obtém tentativas suspeitas (últimas 100)
     */
    @Transactional(readOnly = true)
    public List<TentativaLogin> obterTentativasSuspeitas() {
        return tentativaLoginRepository.findTop100ByOrderByDataTentativaDesc();
    }
}
