package br.com.casadoamor.sgca.service.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.casadoamor.sgca.dto.SessaoDTO;
import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.entity.auth.SessaoUsuario;
import br.com.casadoamor.sgca.exception.ResourceNotFoundException;
import br.com.casadoamor.sgca.repository.auth.SessaoUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para gerenciamento de sessões JWT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessaoService {

    private final SessaoUsuarioRepository sessaoRepository;

    /**
     * Cria nova sessão para o usuário
     */
    @Transactional
    public SessaoUsuario criarSessao(AuthUsuario usuario, String tokenJwt, 
                                     String ipOrigem, String userAgent, 
                                     LocalDateTime expiracao) {
        SessaoUsuario sessao = SessaoUsuario.builder()
                .usuario(usuario)
                .tokenJwt(tokenJwt)
                .ipOrigem(ipOrigem)
                .userAgent(userAgent)
                .expiraEm(expiracao)
                .ativo(true)
                .build();

        sessaoRepository.save(sessao);
        log.info("Nova sessão criada para usuário: {}", usuario.getEmail());
        
        return sessao;
    }

    /**
     * Valida se sessão está ativa
     */
    @Transactional(readOnly = true)
    public boolean sessaoValida(String tokenJwt) {
        return sessaoRepository.findByTokenJwt(tokenJwt)
                .map(sessao -> sessao.getAtivo() && 
                              sessao.getExpiraEm().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    /**
     * Revoga sessão específica
     */
    @Transactional
    public void revogarSessao(Long sessaoId, Long usuarioId) {
        SessaoUsuario sessao = sessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada"));

        if (!sessao.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("Sessão não pertence ao usuário");
        }

        sessao.revogar();
        sessaoRepository.save(sessao);
        log.info("Sessão {} revogada para usuário ID: {}", sessaoId, usuarioId);
    }

    /**
     * Revoga todas as sessões de um usuário (exceto a atual)
     */
    @Transactional
    public void revogarTodasSessoes(Long usuarioId, String tokenAtual) {
        List<SessaoUsuario> sessoes = sessaoRepository.findByUsuarioIdAndAtivo(usuarioId, true);
        
        sessoes.stream()
                .filter(s -> !s.getTokenJwt().equals(tokenAtual))
                .forEach(sessao -> {
                    sessao.revogar();
                    sessaoRepository.save(sessao);
                });

        log.info("Todas as sessões revogadas para usuário ID: {} (exceto sessão atual)", usuarioId);
    }

    /**
     * Revoga TODAS as sessões de um usuário (inclui sessão atual) - Force Logout
     */
    @Transactional
    public void revogarTodasSessoes(Long usuarioId) {
        List<SessaoUsuario> sessoes = sessaoRepository.findByUsuarioIdAndAtivo(usuarioId, true);
        
        sessoes.forEach(sessao -> {
            sessao.revogar();
            sessaoRepository.save(sessao);
        });

        log.info("FORCE LOGOUT: Todas as sessões revogadas para usuário ID: {}", usuarioId);
    }

    /**
     * Lista todas as sessões ativas de um usuário
     */
    @Transactional(readOnly = true)
    public List<SessaoDTO> listarSessoesAtivas(Long usuarioId, String tokenAtual) {
        List<SessaoUsuario> sessoes = sessaoRepository
                .findByUsuarioIdAndAtivoAndExpiraEmAfter(usuarioId, true, LocalDateTime.now());

        return sessoes.stream()
                .map(sessao -> SessaoDTO.builder()
                        .id(sessao.getId())
                        .ipOrigem(sessao.getIpOrigem())
                        .userAgent(sessao.getUserAgent())
                        .criadoEm(sessao.getCriadoEm())
                        .expiraEm(sessao.getExpiraEm())
                        .ativo(sessao.getAtivo())
                        .atual(sessao.getTokenJwt().equals(tokenAtual))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Conta sessões ativas de um usuário
     */
    @Transactional(readOnly = true)
    public Long contarSessoesAtivas(Long usuarioId) {
        return sessaoRepository.countByUsuarioIdAndAtivoAndExpiraEmAfter(
                usuarioId, true, LocalDateTime.now());
    }

    /**
     * Limpa sessões expiradas (método para agendamento)
     */
    @Transactional
    public void limparSessoesExpiradas() {
        List<SessaoUsuario> sessoesExpiradas = sessaoRepository
                .findByAtivoAndExpiraEmAfter(true, LocalDateTime.now());

        sessoesExpiradas.forEach(sessao -> {
            sessao.setAtivo(false);
            sessaoRepository.save(sessao);
        });

        log.info("{} sessões expiradas marcadas como inativas", sessoesExpiradas.size());
    }
}
