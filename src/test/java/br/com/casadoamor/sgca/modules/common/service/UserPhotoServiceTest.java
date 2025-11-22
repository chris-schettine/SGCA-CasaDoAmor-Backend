package br.com.casadoamor.sgca.modules.common.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UserPhotoServiceTest {

    @Mock
    FileStorageService fileStorageService;

    @Mock
    AuthUsuarioRepository authUsuarioRepository;

    @InjectMocks
    UserPhotoService service;

    @Test
    void uploadFoto_userNotFound_throws() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(authUsuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.uploadFoto(1L, file)).isInstanceOf(RuntimeException.class).hasMessageContaining("Usuário não encontrado");
    }

    @Test
    void uploadFoto_emptyFile_throws() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        AuthUsuario u = new AuthUsuario(); u.setId(2L);
        when(authUsuarioRepository.findById(2L)).thenReturn(Optional.of(u));
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> service.uploadFoto(2L, file)).isInstanceOf(IOException.class).hasMessageContaining("Arquivo vazio");
    }

    @Test
    void uploadFoto_happyPath_deletesOldAndSavesNew_returnsUrl() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.png");
        when(file.isEmpty()).thenReturn(false);

        AuthUsuario u = new AuthUsuario(); u.setId(3L); u.setFotoPath("old/path.jpg");
        when(authUsuarioRepository.findById(3L)).thenReturn(Optional.of(u));

        when(fileStorageService.validarTipoArquivo(any(), any(String[].class))).thenReturn(true);
        when(fileStorageService.validarTamanhoArquivo(any(), anyLong())).thenReturn(true);
        when(fileStorageService.salvarArquivo(any(), any(), any())).thenReturn("avatars/3-new.png");
        when(fileStorageService.obterUrlPublica("avatars/3-new.png")).thenReturn("https://cdn/avatars/3-new.png");
        when(authUsuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String url = service.uploadFoto(3L, file);

        assertThat(url).isEqualTo("https://cdn/avatars/3-new.png");
        verify(fileStorageService).deletarArquivo("old/path.jpg");
        verify(authUsuarioRepository).save(u);
        assertThat(u.getFotoPath()).isEqualTo("avatars/3-new.png");
        assertThat(u.getFotoUrl()).isEqualTo("https://cdn/avatars/3-new.png");
        assertThat(u.getFotoAtualizadaEm()).isNotNull();
    }

    @Test
    void deletarFoto_userWithPhoto_deletesAndClears() {
        AuthUsuario u = new AuthUsuario(); u.setId(4L); u.setFotoPath("some/path.jpg"); u.setFotoUrl("url"); u.setFotoAtualizadaEm(LocalDateTime.now());
        when(authUsuarioRepository.findById(4L)).thenReturn(Optional.of(u));
        when(fileStorageService.deletarArquivo("some/path.jpg")).thenReturn(true);
        when(authUsuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.deletarFoto(4L);

        verify(fileStorageService).deletarArquivo("some/path.jpg");
        assertThat(u.getFotoPath()).isNull();
        assertThat(u.getFotoUrl()).isNull();
        assertThat(u.getFotoAtualizadaEm()).isNull();
    }

    @Test
    void obterUrlFoto_returnsValue_orThrows() {
        AuthUsuario u = new AuthUsuario(); u.setId(5L); u.setFotoUrl("uurl");
        when(authUsuarioRepository.findById(5L)).thenReturn(Optional.of(u));

        assertThat(service.obterUrlFoto(5L)).isEqualTo("uurl");

        when(authUsuarioRepository.findById(6L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obterUrlFoto(6L)).isInstanceOf(RuntimeException.class).hasMessageContaining("Usuário não encontrado");
    }
}
