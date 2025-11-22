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
}
