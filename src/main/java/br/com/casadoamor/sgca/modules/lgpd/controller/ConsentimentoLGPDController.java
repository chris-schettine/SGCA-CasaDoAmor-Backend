package br.com.casadoamor.sgca.modules.lgpd.controller;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;
import br.com.casadoamor.sgca.modules.funcionario.entity.Profissional;
import br.com.casadoamor.sgca.modules.funcionario.repository.ProfissionalRepository;
import br.com.casadoamor.sgca.modules.lgpd.dto.ConsentimentoLGPDRequestDTO;
import br.com.casadoamor.sgca.modules.lgpd.dto.ConsentimentoLGPDResponseDTO;
import br.com.casadoamor.sgca.modules.lgpd.entity.ConsentimentoLGPD.TipoEntidadeLGPD;
import br.com.casadoamor.sgca.modules.lgpd.service.ConsentimentoLGPDService;
import br.com.casadoamor.sgca.modules.common.dto.MessageResponseDTO;
import br.com.casadoamor.sgca.infra.util.CpfUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller unificado para gerenciar consentimentos LGPD de todas as entidades
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "LGPD - Consentimentos", description = "Gerenciamento de consentimentos LGPD")
@SecurityRequirement(name = "bearer-jwt")
public class ConsentimentoLGPDController {

    private final ConsentimentoLGPDService consentimentoService;
    private final AuthUsuarioRepository authUsuarioRepository;
    private final ProfissionalRepository profissionalRepository;

    /**
     * Obtém o usuário autenticado a partir do Authentication
     */
    private AuthUsuario getUsuarioAutenticado(Authentication authentication) {
        String cpf = authentication.getName();
        return authUsuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));
    }

    // ==================== ENDPOINTS PARA USUÁRIOS ====================

    @PostMapping("/usuarios/{cpf}/consentimentos-lgpd")
    @PreAuthorize("hasRole('ADMINISTRADOR') or #cpf == authentication.principal.username")
    @Operation(summary = "Registrar consentimento LGPD para usuário")
    public ResponseEntity<?> registrarConsentimentoUsuario(
            @PathVariable String cpf,
            @Valid @RequestBody ConsentimentoLGPDRequestDTO request,
            Authentication authentication
    ) {
        log.info("Registrando consentimento LGPD para usuário CPF: {}", cpf);

        String cpfLimpo = CpfUtil.limparCpf(cpf);
        AuthUsuario usuario = authUsuarioRepository.findByCpf(cpfLimpo)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        AuthUsuario registradoPor = getUsuarioAutenticado(authentication);

        ConsentimentoLGPDResponseDTO response = consentimentoService.registrarConsentimento(
                TipoEntidadeLGPD.USUARIO,
                usuario.getId(),
                request,
                registradoPor
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuarios/{cpf}/consentimentos-lgpd")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR') or #cpf == authentication.principal.username")
    @Operation(summary = "Listar histórico de consentimentos de um usuário")
    public ResponseEntity<List<ConsentimentoLGPDResponseDTO>> listarConsentimentosUsuario(
            @PathVariable String cpf
    ) {
        log.info("Listando consentimentos para usuário CPF: {}", cpf);

        String cpfLimpo = CpfUtil.limparCpf(cpf);
        AuthUsuario usuario = authUsuarioRepository.findByCpf(cpfLimpo)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<ConsentimentoLGPDResponseDTO> consentimentos = consentimentoService.listarConsentimentos(
                TipoEntidadeLGPD.USUARIO,
                usuario.getId()
        );

        return ResponseEntity.ok(consentimentos);
    }

    @GetMapping("/usuarios/{cpf}/consentimentos-lgpd/valido")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR') or #cpf == authentication.principal.username")
    @Operation(summary = "Verificar se usuário possui consentimento válido")
    public ResponseEntity<Map<String, Boolean>> verificarConsentimentoValidoUsuario(
            @PathVariable String cpf
    ) {
        log.info("Verificando consentimento válido para usuário CPF: {}", cpf);

        String cpfLimpo = CpfUtil.limparCpf(cpf);
        AuthUsuario usuario = authUsuarioRepository.findByCpf(cpfLimpo)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        boolean valido = consentimentoService.hasConsentimentoValido(
                TipoEntidadeLGPD.USUARIO,
                usuario.getId()
        );

        return ResponseEntity.ok(Map.of("valido", valido));
    }

    @GetMapping("/usuarios/{cpf}/consentimentos-lgpd/atual")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR') or #cpf == authentication.principal.username")
    @Operation(summary = "Obter consentimento atual de um usuário")
    public ResponseEntity<?> obterConsentimentoAtualUsuario(
            @PathVariable String cpf
    ) {
        log.info("Obtendo consentimento atual para usuário CPF: {}", cpf);

        String cpfLimpo = CpfUtil.limparCpf(cpf);
        AuthUsuario usuario = authUsuarioRepository.findByCpf(cpfLimpo)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        ConsentimentoLGPDResponseDTO consentimento = consentimentoService.obterConsentimentoAtual(
                TipoEntidadeLGPD.USUARIO,
                usuario.getId()
        );

        if (consentimento == null) {
            return ResponseEntity.ok(MessageResponseDTO.success("Nenhum consentimento encontrado"));
        }

        return ResponseEntity.ok(consentimento);
    }

    // ==================== ENDPOINTS PARA PROFISSIONAIS ====================

    @PostMapping("/profissionais/{uuid}/consentimentos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GESTOR_RH')")
    @Operation(summary = "Registrar consentimento LGPD para profissional")
    public ResponseEntity<?> registrarConsentimentoProfissional(
            @PathVariable String uuid,
            @Valid @RequestBody ConsentimentoLGPDRequestDTO request,
            Authentication authentication
    ) {
        log.info("Registrando consentimento LGPD para profissional UUID: {}", uuid);

        Profissional profissional = profissionalRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        AuthUsuario registradoPor = getUsuarioAutenticado(authentication);

        ConsentimentoLGPDResponseDTO response = consentimentoService.registrarConsentimento(
                TipoEntidadeLGPD.PROFISSIONAL,
                profissional.getId(),
                request,
                registradoPor
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profissionais/{uuid}/consentimentos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GESTOR_RH', 'AUDITOR')")
    @Operation(summary = "Listar histórico de consentimentos de um profissional")
    public ResponseEntity<List<ConsentimentoLGPDResponseDTO>> listarConsentimentosProfissional(
            @PathVariable String uuid
    ) {
        log.info("Listando consentimentos para profissional UUID: {}", uuid);

        Profissional profissional = profissionalRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        List<ConsentimentoLGPDResponseDTO> consentimentos = consentimentoService.listarConsentimentos(
                TipoEntidadeLGPD.PROFISSIONAL,
                profissional.getId()
        );

        return ResponseEntity.ok(consentimentos);
    }

    @GetMapping("/profissionais/{uuid}/consentimentos/valido")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GESTOR_RH', 'AUDITOR')")
    @Operation(summary = "Verificar se profissional possui consentimento válido")
    public ResponseEntity<Map<String, Boolean>> verificarConsentimentoValidoProfissional(
            @PathVariable String uuid
    ) {
        log.info("Verificando consentimento válido para profissional UUID: {}", uuid);

        Profissional profissional = profissionalRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        boolean valido = consentimentoService.hasConsentimentoValido(
                TipoEntidadeLGPD.PROFISSIONAL,
                profissional.getId()
        );

        return ResponseEntity.ok(Map.of("valido", valido));
    }

    @GetMapping("/profissionais/{uuid}/consentimentos/atual")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GESTOR_RH', 'AUDITOR')")
    @Operation(summary = "Obter consentimento atual de um profissional")
    public ResponseEntity<?> obterConsentimentoAtualProfissional(
            @PathVariable String uuid
    ) {
        log.info("Obtendo consentimento atual para profissional UUID: {}", uuid);

        Profissional profissional = profissionalRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        ConsentimentoLGPDResponseDTO consentimento = consentimentoService.obterConsentimentoAtual(
                TipoEntidadeLGPD.PROFISSIONAL,
                profissional.getId()
        );

        if (consentimento == null) {
            return ResponseEntity.ok(MessageResponseDTO.success("Nenhum consentimento encontrado"));
        }

        return ResponseEntity.ok(consentimento);
    }

    // ==================== ENDPOINTS ADMINISTRATIVOS ====================

    @GetMapping("/consentimentos-lgpd/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    @Operation(summary = "Listar consentimentos por tipo de entidade")
    public ResponseEntity<List<ConsentimentoLGPDResponseDTO>> listarPorTipo(
            @PathVariable String tipo
    ) {
        log.info("Listando consentimentos por tipo: {}", tipo);

        TipoEntidadeLGPD tipoEntidade = TipoEntidadeLGPD.valueOf(tipo.toUpperCase());
        List<ConsentimentoLGPDResponseDTO> consentimentos = consentimentoService.listarPorTipo(tipoEntidade);

        return ResponseEntity.ok(consentimentos);
    }

    @GetMapping("/consentimentos-lgpd/versao/{versao}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    @Operation(summary = "Listar consentimentos por versão do termo")
    public ResponseEntity<List<ConsentimentoLGPDResponseDTO>> listarPorVersao(
            @PathVariable String versao
    ) {
        log.info("Listando consentimentos por versão: {}", versao);

        List<ConsentimentoLGPDResponseDTO> consentimentos = consentimentoService.listarPorVersao(versao);
        return ResponseEntity.ok(consentimentos);
    }

    @GetMapping("/consentimentos-lgpd/estatisticas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    @Operation(summary = "Obter estatísticas de consentimentos")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        log.info("Obtendo estatísticas de consentimentos LGPD");

        Long usuariosConcordam = consentimentoService.contarConsentimentos(TipoEntidadeLGPD.USUARIO, true);
        Long usuariosNaoConcordam = consentimentoService.contarConsentimentos(TipoEntidadeLGPD.USUARIO, false);
        Long profissionaisConcordam = consentimentoService.contarConsentimentos(TipoEntidadeLGPD.PROFISSIONAL, true);
        Long profissionaisNaoConcordam = consentimentoService.contarConsentimentos(TipoEntidadeLGPD.PROFISSIONAL, false);

        Map<String, Object> estatisticas = Map.of(
                "usuarios", Map.of(
                        "concordam", usuariosConcordam,
                        "naoConcordam", usuariosNaoConcordam,
                        "total", usuariosConcordam + usuariosNaoConcordam
                ),
                "profissionais", Map.of(
                        "concordam", profissionaisConcordam,
                        "naoConcordam", profissionaisNaoConcordam,
                        "total", profissionaisConcordam + profissionaisNaoConcordam
                ),
                "totalGeral", usuariosConcordam + usuariosNaoConcordam + 
                             profissionaisConcordam + profissionaisNaoConcordam
        );

        return ResponseEntity.ok(estatisticas);
    }
}
