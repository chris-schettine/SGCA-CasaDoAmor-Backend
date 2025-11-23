package br.com.casadoamor.sgca.modules.common.controller;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FileController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class FileControllerIT {

    @Autowired
    private MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.common.service.FileStorageService fileStorageService;

    // security beans
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void serves_file_when_exists() throws Exception {
        // create temporary file to simulate storage
        Path tmp = Files.createTempFile("test-file", ".jpg");
        Files.writeString(tmp, "fake-image-bytes");

        String sub = "avatars";
        String name = "user123.jpg";
        String filePath = sub + "/" + name;

        org.mockito.Mockito.when(fileStorageService.arquivoExiste(filePath)).thenReturn(true);
        org.mockito.Mockito.when(fileStorageService.obterCaminhoAbsoluto(filePath)).thenReturn(tmp);

        var response = mvc.perform(get("/files/" + sub + "/" + name).contentType(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        assertThat(response.getContentType()).isNotNull();
        assertThat(response.getHeader("Content-Disposition")).contains("inline");
        Files.deleteIfExists(tmp);
    }

    @Test
    void returns_404_when_not_found() throws Exception {
        String sub = "avatars";
        String name = "missing.jpg";
        org.mockito.Mockito.when(fileStorageService.arquivoExiste(org.mockito.ArgumentMatchers.eq(sub + "/" + name))).thenReturn(false);

        mvc.perform(get("/files/" + sub + "/" + name))
            .andExpect(status().isNotFound());
    }
}
