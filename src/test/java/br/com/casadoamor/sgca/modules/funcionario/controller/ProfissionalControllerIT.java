package br.com.casadoamor.sgca.modules.funcionario.controller;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalRequestDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResponseDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResumoDTO;

@WebMvcTest(controllers = ProfissionalController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ProfissionalControllerIT {

    @Autowired
    private MockMvc mvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.funcionario.service.ProfissionalService profissionalService;

    // JwtAuthenticationFilter dependencies
    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.JwtUtil jwtUtil;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.infra.security.UserDetailsServiceImpl userDetailsServiceImpl;

    @org.springframework.boot.test.mock.mockito.MockBean
    private br.com.casadoamor.sgca.modules.admin.service.SessaoService sessaoService;

    @Test
    void cadastrar_and_buscarPorUuid_and_listarTodos() throws Exception {
        ProfissionalRequestDTO req = ProfissionalRequestDTO.builder()
                .nome("Dr Test")
                .cpf("11122233344")
                .categoria(br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional.MEDICO)
                .dataAdmissao(LocalDate.now())
                .build();

        ProfissionalResponseDTO resp = ProfissionalResponseDTO.builder().uuid("p1").nome("Dr Test").email("doc@example.com").build();
        org.mockito.Mockito.when(profissionalService.cadastrar(org.mockito.ArgumentMatchers.any(ProfissionalRequestDTO.class), org.mockito.ArgumentMatchers.any()))
                .thenReturn(resp);

        // Authentication principal
        var user = br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario.builder().id(1L).cpf("11122233344").build();
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.TestingAuthenticationToken(user, null);

        String created = mvc.perform(post("/api/profissionais").principal(auth).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Dr Test\",\"cpf\":\"11122233344\",\"categoria\":\"MEDICO\",\"dataAdmissao\":\"2025-01-01\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        assertThat(created).contains("p1");

        // buscar por uuid
        org.mockito.Mockito.when(profissionalService.buscarPorUuid(org.mockito.ArgumentMatchers.eq("p1"))).thenReturn(resp);

        String found = mvc.perform(get("/api/profissionais/p1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(found).contains("p1");

        // listar todos (resumo)
        ProfissionalResumoDTO resumo = ProfissionalResumoDTO.builder().uuid("p1").nome("Dr Test").build();
        org.mockito.Mockito.when(profissionalService.listarTodos()).thenReturn(List.of(resumo));

        String listJson = mvc.perform(get("/api/profissionais").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(listJson).contains("Dr Test");
    }

    @Test
    void listar_por_categoria_and_listar_ativos() throws Exception {
        br.com.casadoamor.sgca.modules.funcionario.dto.CategoriaProfissionalDTO categoriaDTO = 
            new br.com.casadoamor.sgca.modules.funcionario.dto.CategoriaProfissionalDTO("MEDICO", "Médico");
        ProfissionalResumoDTO resumoCategoria = ProfissionalResumoDTO.builder().uuid("p2").nome("Dr Category").categoria(categoriaDTO).build();
        org.mockito.Mockito.when(profissionalService.listarPorCategoria(org.mockito.ArgumentMatchers.eq(br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional.MEDICO))).thenReturn(List.of(resumoCategoria));

        String byCategory = mvc.perform(get("/api/profissionais/categoria/MEDICO").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(byCategory).contains("Dr Category");

        ProfissionalResumoDTO ativo = ProfissionalResumoDTO.builder().uuid("p3").nome("Active Doc").ativo(true).build();
        org.mockito.Mockito.when(profissionalService.listarAtivos()).thenReturn(List.of(ativo));

        String ativosJson = mvc.perform(get("/api/profissionais/ativos").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(ativosJson).contains("Active Doc");
    }
}
