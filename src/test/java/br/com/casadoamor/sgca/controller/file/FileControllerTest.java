package br.com.casadoamor.sgca.controller.file;

import br.com.casadoamor.sgca.service.file.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FileControllerTest {

    @Mock
    private FileStorageService storageService;

    @InjectMocks
    private FileController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void servirArquivo_NotFound_WhenArquivoNaoExiste() {
        when(storageService.arquivoExiste("avatars/notfound.jpg")).thenReturn(false);

        ResponseEntity<Resource> res = controller.servirArquivo("avatars", "notfound.jpg");

        assertThat(res.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void servirArquivo_InternalServerError_OnException() {
        when(storageService.arquivoExiste("avatars/error.jpg")).thenReturn(true);
        when(storageService.obterCaminhoAbsoluto("avatars/error.jpg")).thenThrow(new RuntimeException("boom"));

        ResponseEntity<Resource> res = controller.servirArquivo("avatars", "error.jpg");

        assertThat(res.getStatusCodeValue()).isEqualTo(500);
    }
}
