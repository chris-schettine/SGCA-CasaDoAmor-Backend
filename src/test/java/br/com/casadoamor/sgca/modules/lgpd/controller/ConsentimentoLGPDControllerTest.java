package br.com.casadoamor.sgca.modules.lgpd.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;
import br.com.casadoamor.sgca.modules.common.dto.MessageResponseDTO;
import br.com.casadoamor.sgca.modules.funcionario.entity.Profissional;
import br.com.casadoamor.sgca.modules.funcionario.repository.ProfissionalRepository;
import br.com.casadoamor.sgca.modules.lgpd.dto.ConsentimentoLGPDRequestDTO;
import br.com.casadoamor.sgca.modules.lgpd.dto.ConsentimentoLGPDResponseDTO;
import br.com.casadoamor.sgca.modules.lgpd.entity.ConsentimentoLGPD.TipoEntidadeLGPD;
import br.com.casadoamor.sgca.modules.lgpd.service.ConsentimentoLGPDService;

@ExtendWith(MockitoExtension.class)
class ConsentimentoLGPDControllerTest {

    @Mock
    private ConsentimentoLGPDService service;

    @Mock
    private AuthUsuarioRepository authRepo;

    @Mock
    private ProfissionalRepository profissionalRepo;

    @InjectMocks
    private ConsentimentoLGPDController controller;

    @Mock
    private Authentication authentication;

    @Test
    void registrarConsentimentoUsuario_whenUsuarioFound_callsServiceAndReturnsOk() {
        // Arrange
        String cpf = "12345678901";
        ConsentimentoLGPDRequestDTO request = ConsentimentoLGPDRequestDTO.builder()
                .versaoTermo("v1").concorda(true).build();

        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(11L);
        usuario.setCpf(cpf);

        AuthUsuario registradoPor = new AuthUsuario();
        registradoPor.setId(22L);

        when(authRepo.findByCpf(cpf)).thenReturn(Optional.of(usuario));
        when(authentication.getName()).thenReturn(cpf);
        when(authRepo.findByCpf(cpf)).thenReturn(Optional.of(usuario));

        ConsentimentoLGPDResponseDTO responseDTO = ConsentimentoLGPDResponseDTO.builder()
                .id(55L).uuid("u55").versaoTermo("v1").build();

        when(service.registrarConsentimento(eq(TipoEntidadeLGPD.USUARIO), eq(usuario.getId()), eq(request), any(AuthUsuario.class)))
                .thenReturn(responseDTO);

        when(authRepo.findByCpf(cpf)).thenReturn(Optional.of(usuario));
        when(authentication.getName()).thenReturn(cpf);

        // Act
        when(authRepo.findByCpf(cpf)).thenReturn(Optional.of(usuario));
        when(authentication.getName()).thenReturn(cpf);

        ResponseEntity<?> resp = controller.registrarConsentimentoUsuario(cpf, request, authentication);

        // Assert
        assertThat(resp.getBody()).isInstanceOf(ConsentimentoLGPDResponseDTO.class);
        ConsentimentoLGPDResponseDTO returned = (ConsentimentoLGPDResponseDTO) resp.getBody();
        assertThat(returned.getId()).isEqualTo(55L);
        verify(service).registrarConsentimento(eq(TipoEntidadeLGPD.USUARIO), eq(usuario.getId()), eq(request), any(AuthUsuario.class));
    }

    @Test
    void registrarConsentimentoUsuario_whenUsuarioNotFound_throws() {
        String cpf = "00000000000";
        when(authRepo.findByCpf(cpf)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> controller.registrarConsentimentoUsuario(cpf, new ConsentimentoLGPDRequestDTO(), authentication));
    }

    @Test
    void listarConsentimentosUsuario_whenFound_returnsList() {
        String cpf = "11122233344";
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(9L);
        usuario.setCpf(cpf);
        when(authRepo.findByCpf(cpf)).thenReturn(Optional.of(usuario));

        when(service.listarConsentimentos(TipoEntidadeLGPD.USUARIO, usuario.getId()))
                .thenReturn(List.of(ConsentimentoLGPDResponseDTO.builder().id(1L).build()));

        ResponseEntity<List<ConsentimentoLGPDResponseDTO>> resp = controller.listarConsentimentosUsuario(cpf);

        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    void verificarConsentimentoValidoUsuario_returnsMap() {
        String cpf = "11122233344";
        AuthUsuario usuario = new AuthUsuario(); usuario.setId(2L); usuario.setCpf(cpf);
        when(authRepo.findByCpf(cpf)).thenReturn(Optional.of(usuario));
        when(service.hasConsentimentoValido(TipoEntidadeLGPD.USUARIO, usuario.getId())).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> resp = controller.verificarConsentimentoValidoUsuario(cpf);
        assertThat(resp.getBody()).containsEntry("valido", true);
    }

    @Test
    void obterConsentimentoAtualUsuario_whenNull_returnsMessage() {
        String cpf = "22233344455";
        AuthUsuario usuario = new AuthUsuario(); usuario.setId(3L); usuario.setCpf(cpf);
        when(authRepo.findByCpf(cpf)).thenReturn(Optional.of(usuario));
        when(service.obterConsentimentoAtual(TipoEntidadeLGPD.USUARIO, usuario.getId())).thenReturn(null);

        ResponseEntity<?> resp = controller.obterConsentimentoAtualUsuario(cpf);
        assertThat(resp.getBody()).isInstanceOf(MessageResponseDTO.class);
        MessageResponseDTO m = (MessageResponseDTO) resp.getBody();
        assertThat(m.getSuccess()).isTrue();
    }

    @Test
    void registrarConsentimentoProfissional_whenProfissionalExists_callsServiceWithSameMetadata() {
        String uuid = "uuid-123";
        Profissional p = new Profissional(); p.setId(88L);
        when(profissionalRepo.findByUuid(uuid)).thenReturn(Optional.of(p));

        AuthUsuario registrado = new AuthUsuario(); registrado.setId(2L);
        when(authentication.getName()).thenReturn("2");
        when(authRepo.findByCpf("2")).thenReturn(Optional.of(registrado));

        ConsentimentoLGPDRequestDTO req = ConsentimentoLGPDRequestDTO.builder()
                .versaoTermo("v1").concorda(true).metadata("{\"k\":\"v\"}").build();

        ConsentimentoLGPDResponseDTO out = ConsentimentoLGPDResponseDTO.builder().id(10L).build();
        when(service.registrarConsentimento(eq(TipoEntidadeLGPD.PROFISSIONAL), eq(88L), eq(req), any(AuthUsuario.class)))
                .thenReturn(out);

        ResponseEntity<?> resp = controller.registrarConsentimentoProfissional(uuid, req, authentication);
        assertThat(resp.getBody()).isInstanceOf(ConsentimentoLGPDResponseDTO.class);
        verify(service).registrarConsentimento(eq(TipoEntidadeLGPD.PROFISSIONAL), eq(88L), eq(req), any());
    }

    @Test
    void registrarConsentimentoProfissional_whenNotFound_andMetadataNull_setsPendingUuidAndUsesNegativeId() {
        String uuid = "not-found-1";
        when(profissionalRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        AuthUsuario registrado = new AuthUsuario(); registrado.setId(99L);
        when(authentication.getName()).thenReturn("99");
        when(authRepo.findByCpf("99")).thenReturn(Optional.of(registrado));

        ConsentimentoLGPDRequestDTO req = ConsentimentoLGPDRequestDTO.builder()
                .versaoTermo("vA").concorda(true).metadata(null).build();

        ArgumentCaptor<ConsentimentoLGPDRequestDTO> captor = ArgumentCaptor.forClass(ConsentimentoLGPDRequestDTO.class);

        when(service.registrarConsentimento(eq(TipoEntidadeLGPD.PROFISSIONAL), anyLong(), captor.capture(), any(AuthUsuario.class)))
                .thenReturn(ConsentimentoLGPDResponseDTO.builder().id(5L).build());

        ResponseEntity<?> resp = controller.registrarConsentimentoProfissional(uuid, req, authentication);

        assertThat(resp.getBody()).isInstanceOf(ConsentimentoLGPDResponseDTO.class);
        ConsentimentoLGPDRequestDTO passed = captor.getValue();
        assertThat(passed.getMetadata()).contains("pendingProfessionalUuid");
        // ensure the generated id is negative
        verify(service).registrarConsentimento(eq(TipoEntidadeLGPD.PROFISSIONAL), argThat(id -> id < 0), any(), any());
    }

    @Test
    void listarPorTipo_and_listarPorVersao_and_estatisticas() {
        // listarPorTipo
        when(service.listarPorTipo(TipoEntidadeLGPD.USUARIO)).thenReturn(List.of());
        ResponseEntity<List<ConsentimentoLGPDResponseDTO>> r1 = controller.listarPorTipo("usuario");
        assertThat(r1.getBody()).isEmpty();

        // listarPorVersao
        when(service.listarPorVersao("vX")).thenReturn(List.of());
        ResponseEntity<List<ConsentimentoLGPDResponseDTO>> r2 = controller.listarPorVersao("vX");
        assertThat(r2.getBody()).isEmpty();

        // estatisticas
        when(service.contarConsentimentos(TipoEntidadeLGPD.USUARIO, true)).thenReturn(2L);
        when(service.contarConsentimentos(TipoEntidadeLGPD.USUARIO, false)).thenReturn(3L);
        when(service.contarConsentimentos(TipoEntidadeLGPD.PROFISSIONAL, true)).thenReturn(7L);
        when(service.contarConsentimentos(TipoEntidadeLGPD.PROFISSIONAL, false)).thenReturn(1L);

        ResponseEntity<Map<String, Object>> stats = controller.obterEstatisticas();
        assertThat(stats.getBody()).containsKeys("usuarios", "profissionais", "totalGeral");
        Map<String, Object> usuarios = (Map<String, Object>) stats.getBody().get("usuarios");
        assertThat(usuarios.get("total")).isEqualTo(5L);
        assertThat(stats.getBody().get("totalGeral")).isEqualTo(13L);
    }

    // ==================== TESTES ADICIONAIS PARA COBERTURA ====================

    @Test
    void obterConsentimentoAtualUsuario_whenConsentimentoExists_returnsConsentimento() {
        String cpf = "33344455566";
        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(4L);
        usuario.setCpf(cpf);

        ConsentimentoLGPDResponseDTO consentimento = ConsentimentoLGPDResponseDTO.builder()
                .id(100L)
                .uuid("consent-uuid")
                .versaoTermo("v2.0")
                .concorda(true)
                .build();

        when(authRepo.findByCpf(cpf)).thenReturn(Optional.of(usuario));
        when(service.obterConsentimentoAtual(TipoEntidadeLGPD.USUARIO, usuario.getId())).thenReturn(consentimento);

        ResponseEntity<?> resp = controller.obterConsentimentoAtualUsuario(cpf);

        assertThat(resp.getBody()).isInstanceOf(ConsentimentoLGPDResponseDTO.class);
        ConsentimentoLGPDResponseDTO returned = (ConsentimentoLGPDResponseDTO) resp.getBody();
        assertThat(returned.getId()).isEqualTo(100L);
        assertThat(returned.getVersaoTermo()).isEqualTo("v2.0");
    }

    @Test
    void registrarConsentimentoProfissional_whenNotFound_andMetadataIsJsonObject_mergesUuid() {
        String uuid = "uuid-not-found-json";
        when(profissionalRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        AuthUsuario registrado = new AuthUsuario();
        registrado.setId(50L);
        when(authentication.getName()).thenReturn("50");
        when(authRepo.findByCpf("50")).thenReturn(Optional.of(registrado));

        // Request com metadata que já é um JSON objeto
        ConsentimentoLGPDRequestDTO req = ConsentimentoLGPDRequestDTO.builder()
                .versaoTermo("v3").concorda(true).metadata("{\"existingKey\":\"existingValue\"}").build();

        ArgumentCaptor<ConsentimentoLGPDRequestDTO> captor = ArgumentCaptor.forClass(ConsentimentoLGPDRequestDTO.class);

        when(service.registrarConsentimento(eq(TipoEntidadeLGPD.PROFISSIONAL), anyLong(), captor.capture(), any(AuthUsuario.class)))
                .thenReturn(ConsentimentoLGPDResponseDTO.builder().id(15L).build());

        controller.registrarConsentimentoProfissional(uuid, req, authentication);

        ConsentimentoLGPDRequestDTO passed = captor.getValue();
        // Deve mesclar o pendingProfessionalUuid no objeto JSON existente
        assertThat(passed.getMetadata()).contains("pendingProfessionalUuid");
        assertThat(passed.getMetadata()).contains("existingKey");
        assertThat(passed.getMetadata()).contains("existingValue");
    }

    @Test
    void registrarConsentimentoProfissional_whenNotFound_andMetadataIsNotJsonObject_wrapsInNewObject() {
        String uuid = "uuid-not-found-raw";
        when(profissionalRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        AuthUsuario registrado = new AuthUsuario();
        registrado.setId(60L);
        when(authentication.getName()).thenReturn("60");
        when(authRepo.findByCpf("60")).thenReturn(Optional.of(registrado));

        // Request com metadata que NÃO é um JSON objeto (string simples)
        ConsentimentoLGPDRequestDTO req = ConsentimentoLGPDRequestDTO.builder()
                .versaoTermo("v4").concorda(true).metadata("raw string metadata").build();

        ArgumentCaptor<ConsentimentoLGPDRequestDTO> captor = ArgumentCaptor.forClass(ConsentimentoLGPDRequestDTO.class);

        when(service.registrarConsentimento(eq(TipoEntidadeLGPD.PROFISSIONAL), anyLong(), captor.capture(), any(AuthUsuario.class)))
                .thenReturn(ConsentimentoLGPDResponseDTO.builder().id(16L).build());

        controller.registrarConsentimentoProfissional(uuid, req, authentication);

        ConsentimentoLGPDRequestDTO passed = captor.getValue();
        // Deve encapsular em um novo objeto JSON
        assertThat(passed.getMetadata()).startsWith("{");
        assertThat(passed.getMetadata()).contains("pendingProfessionalUuid");
        assertThat(passed.getMetadata()).contains("raw");
    }

    @Test
    void listarConsentimentosProfissional_whenFound_returnsList() {
        String uuid = "prof-uuid-list";
        Profissional profissional = new Profissional();
        profissional.setId(77L);

        when(profissionalRepo.findByUuid(uuid)).thenReturn(Optional.of(profissional));

        ConsentimentoLGPDResponseDTO c1 = ConsentimentoLGPDResponseDTO.builder().id(1L).build();
        ConsentimentoLGPDResponseDTO c2 = ConsentimentoLGPDResponseDTO.builder().id(2L).build();
        when(service.listarConsentimentos(TipoEntidadeLGPD.PROFISSIONAL, profissional.getId()))
                .thenReturn(List.of(c1, c2));

        ResponseEntity<List<ConsentimentoLGPDResponseDTO>> resp = controller.listarConsentimentosProfissional(uuid);

        assertThat(resp.getBody()).hasSize(2);
        verify(service).listarConsentimentos(TipoEntidadeLGPD.PROFISSIONAL, 77L);
    }

    @Test
    void listarConsentimentosProfissional_whenNotFound_throws() {
        String uuid = "non-existent-prof";
        when(profissionalRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> controller.listarConsentimentosProfissional(uuid));
    }

    @Test
    void verificarConsentimentoValidoProfissional_whenValid_returnsTrue() {
        String uuid = "prof-uuid-valid";
        Profissional profissional = new Profissional();
        profissional.setId(88L);

        when(profissionalRepo.findByUuid(uuid)).thenReturn(Optional.of(profissional));
        when(service.hasConsentimentoValido(TipoEntidadeLGPD.PROFISSIONAL, profissional.getId())).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> resp = controller.verificarConsentimentoValidoProfissional(uuid);

        assertThat(resp.getBody()).containsEntry("valido", true);
    }

    @Test
    void verificarConsentimentoValidoProfissional_whenInvalid_returnsFalse() {
        String uuid = "prof-uuid-invalid";
        Profissional profissional = new Profissional();
        profissional.setId(89L);

        when(profissionalRepo.findByUuid(uuid)).thenReturn(Optional.of(profissional));
        when(service.hasConsentimentoValido(TipoEntidadeLGPD.PROFISSIONAL, profissional.getId())).thenReturn(false);

        ResponseEntity<Map<String, Boolean>> resp = controller.verificarConsentimentoValidoProfissional(uuid);

        assertThat(resp.getBody()).containsEntry("valido", false);
    }

    @Test
    void verificarConsentimentoValidoProfissional_whenNotFound_throws() {
        String uuid = "non-existent-prof-2";
        when(profissionalRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> controller.verificarConsentimentoValidoProfissional(uuid));
    }

    @Test
    void obterConsentimentoAtualProfissional_whenNull_returnsMessage() {
        String uuid = "prof-uuid-null";
        Profissional profissional = new Profissional();
        profissional.setId(90L);

        when(profissionalRepo.findByUuid(uuid)).thenReturn(Optional.of(profissional));
        when(service.obterConsentimentoAtual(TipoEntidadeLGPD.PROFISSIONAL, profissional.getId())).thenReturn(null);

        ResponseEntity<?> resp = controller.obterConsentimentoAtualProfissional(uuid);

        assertThat(resp.getBody()).isInstanceOf(MessageResponseDTO.class);
        MessageResponseDTO m = (MessageResponseDTO) resp.getBody();
        assertThat(m.getSuccess()).isTrue();
        assertThat(m.getMessage()).contains("Nenhum consentimento encontrado");
    }

    @Test
    void obterConsentimentoAtualProfissional_whenExists_returnsConsentimento() {
        String uuid = "prof-uuid-exists";
        Profissional profissional = new Profissional();
        profissional.setId(91L);

        ConsentimentoLGPDResponseDTO consentimento = ConsentimentoLGPDResponseDTO.builder()
                .id(200L)
                .uuid("consent-prof-uuid")
                .versaoTermo("v5.0")
                .concorda(true)
                .build();

        when(profissionalRepo.findByUuid(uuid)).thenReturn(Optional.of(profissional));
        when(service.obterConsentimentoAtual(TipoEntidadeLGPD.PROFISSIONAL, profissional.getId())).thenReturn(consentimento);

        ResponseEntity<?> resp = controller.obterConsentimentoAtualProfissional(uuid);

        assertThat(resp.getBody()).isInstanceOf(ConsentimentoLGPDResponseDTO.class);
        ConsentimentoLGPDResponseDTO returned = (ConsentimentoLGPDResponseDTO) resp.getBody();
        assertThat(returned.getId()).isEqualTo(200L);
        assertThat(returned.getVersaoTermo()).isEqualTo("v5.0");
    }

    @Test
    void obterConsentimentoAtualProfissional_whenNotFound_throws() {
        String uuid = "non-existent-prof-3";
        when(profissionalRepo.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> controller.obterConsentimentoAtualProfissional(uuid));
    }

    @Test
    void listarConsentimentosUsuario_whenNotFound_throws() {
        String cpf = "00000000001";
        when(authRepo.findByCpf(cpf)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> controller.listarConsentimentosUsuario(cpf));
    }

    @Test
    void verificarConsentimentoValidoUsuario_whenNotFound_throws() {
        String cpf = "00000000002";
        when(authRepo.findByCpf(cpf)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> controller.verificarConsentimentoValidoUsuario(cpf));
    }

    @Test
    void obterConsentimentoAtualUsuario_whenNotFound_throws() {
        String cpf = "00000000003";
        when(authRepo.findByCpf(cpf)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> controller.obterConsentimentoAtualUsuario(cpf));
    }

    @SuppressWarnings("unchecked")
    @Test
    void listarPorTipo_withProfissional_returnsList() {
        ConsentimentoLGPDResponseDTO c1 = ConsentimentoLGPDResponseDTO.builder().id(1L).build();
        when(service.listarPorTipo(TipoEntidadeLGPD.PROFISSIONAL)).thenReturn(List.of(c1));

        ResponseEntity<List<ConsentimentoLGPDResponseDTO>> resp = controller.listarPorTipo("profissional");

        assertThat(resp.getBody()).hasSize(1);
    }
}
