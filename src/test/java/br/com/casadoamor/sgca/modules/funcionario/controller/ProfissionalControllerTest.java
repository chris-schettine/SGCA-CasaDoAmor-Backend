package br.com.casadoamor.sgca.modules.funcionario.controller;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.funcionario.dto.CategoriaProfissionalDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.DashboardStatsDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalRequestDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResponseDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResumoDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.TipoVinculoDTO;
import br.com.casadoamor.sgca.modules.funcionario.entity.TipoVinculoEntity;
import br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional;
import br.com.casadoamor.sgca.modules.funcionario.repository.TipoVinculoRepository;
import br.com.casadoamor.sgca.modules.funcionario.service.ProfissionalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfissionalControllerTest {

    @Mock
    private ProfissionalService profissionalService;

    @Mock
    private TipoVinculoRepository tipoVinculoRepository;

    @InjectMocks
    private ProfissionalController profissionalController;

    private AuthUsuario usuarioLogado;
    private ProfissionalRequestDTO requestDTO;
    private ProfissionalResponseDTO responseDTO;
    private ProfissionalResumoDTO resumoDTO;

    @BeforeEach
    void setUp() {
        usuarioLogado = new AuthUsuario();
        usuarioLogado.setCpf("12345678900");

        requestDTO = ProfissionalRequestDTO.builder()
                .nome("João Silva")
                .cpf("111.222.333-44")
                .email("joao@test.com")
                .build();

        responseDTO = ProfissionalResponseDTO.builder()
                .uuid("uuid-123")
                .nome("João Silva")
                .cpf("11122233344")
                .email("joao@test.com")
                .ativo(true)
                .build();

        resumoDTO = ProfissionalResumoDTO.builder()
                .uuid("uuid-123")
                .nome("João Silva")
                .cpf("11122233344")
                .ativo(true)
                .build();
    }

    @Nested
    @DisplayName("Testes de Cadastro")
    class CadastroTests {

        @Test
        @DisplayName("Deve cadastrar profissional com sucesso")
        void deveCadastrarProfissionalComSucesso() {
            when(profissionalService.cadastrar(requestDTO, usuarioLogado)).thenReturn(responseDTO);

            ResponseEntity<ProfissionalResponseDTO> response = profissionalController.cadastrar(requestDTO, usuarioLogado);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("uuid-123", response.getBody().getUuid());
            verify(profissionalService).cadastrar(requestDTO, usuarioLogado);
        }
    }

    @Nested
    @DisplayName("Testes de Atualização")
    class AtualizacaoTests {

        @Test
        @DisplayName("Deve atualizar profissional com sucesso")
        void deveAtualizarProfissionalComSucesso() {
            String uuid = "uuid-123";
            when(profissionalService.atualizar(uuid, requestDTO, usuarioLogado)).thenReturn(responseDTO);

            ResponseEntity<ProfissionalResponseDTO> response = profissionalController.atualizar(uuid, requestDTO, usuarioLogado);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(uuid, response.getBody().getUuid());
            verify(profissionalService).atualizar(uuid, requestDTO, usuarioLogado);
        }
    }

    @Nested
    @DisplayName("Testes de Busca por UUID")
    class BuscaPorUuidTests {

        @Test
        @DisplayName("Deve buscar profissional por UUID com sucesso")
        void deveBuscarPorUuidComSucesso() {
            String uuid = "uuid-123";
            when(profissionalService.buscarPorUuid(uuid)).thenReturn(responseDTO);

            ResponseEntity<ProfissionalResponseDTO> response = profissionalController.buscarPorUuid(uuid);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(uuid, response.getBody().getUuid());
        }
    }

    @Nested
    @DisplayName("Testes de Listagem")
    class ListagemTests {

        @Test
        @DisplayName("Deve listar todos os profissionais sem parâmetros")
        void deveListarTodosSemParametros() {
            List<ProfissionalResumoDTO> lista = List.of(resumoDTO);
            doReturn(lista).when(profissionalService).listarTodos();

            ResponseEntity<List<ProfissionalResumoDTO>> response = profissionalController.listarTodos(null, false);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
            verify(profissionalService).listarTodos();
        }

        @Test
        @DisplayName("Deve listar apenas ativos sem termo")
        void deveListarApenasAtivosSemTermo() {
            List<ProfissionalResumoDTO> lista = List.of(resumoDTO);
            doReturn(lista).when(profissionalService).listarAtivos();

            ResponseEntity<List<ProfissionalResumoDTO>> response = profissionalController.listarTodos(null, true);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(profissionalService).listarAtivos();
        }

        @Test
        @DisplayName("Deve buscar com termo e apenas ativos")
        void deveBuscarComTermoApenasAtivos() {
            String termo = "João";
            List<ProfissionalResumoDTO> lista = List.of(resumoDTO);
            doReturn(lista).when(profissionalService).buscarAtivosPorMultiplosCampos(termo);

            ResponseEntity<List<ProfissionalResumoDTO>> response = profissionalController.listarTodos(termo, true);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(profissionalService).buscarAtivosPorMultiplosCampos(termo);
        }

        @Test
        @DisplayName("Deve buscar com termo incluindo inativos")
        void deveBuscarComTermoIncluindoInativos() {
            String termo = "João";
            List<ProfissionalResumoDTO> lista = List.of(resumoDTO);
            doReturn(lista).when(profissionalService).buscarPorMultiplosCampos(termo);

            ResponseEntity<List<ProfissionalResumoDTO>> response = profissionalController.listarTodos(termo, false);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(profissionalService).buscarPorMultiplosCampos(termo);
        }

        @Test
        @DisplayName("Deve listar por categoria")
        void deveListarPorCategoria() {
            CategoriaProfissional categoria = CategoriaProfissional.MEDICO;
            List<ProfissionalResumoDTO> lista = List.of(resumoDTO);
            doReturn(lista).when(profissionalService).listarPorCategoria(categoria);

            ResponseEntity<List<ProfissionalResumoDTO>> response = profissionalController.listarPorCategoria(categoria);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(profissionalService).listarPorCategoria(categoria);
        }

        @Test
        @DisplayName("Deve listar ativos")
        void deveListarAtivos() {
            List<ProfissionalResumoDTO> lista = List.of(resumoDTO);
            doReturn(lista).when(profissionalService).listarAtivos();

            ResponseEntity<List<ProfissionalResumoDTO>> response = profissionalController.listarAtivos();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(profissionalService).listarAtivos();
        }

        @Test
        @DisplayName("Deve buscar por nome")
        void deveBuscarPorNome() {
            String nome = "João";
            List<ProfissionalResumoDTO> lista = List.of(resumoDTO);
            doReturn(lista).when(profissionalService).buscarPorNome(nome);

            ResponseEntity<List<ProfissionalResumoDTO>> response = profissionalController.buscarPorNome(nome);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(profissionalService).buscarPorNome(nome);
        }
    }

    @Nested
    @DisplayName("Testes de Pesquisa")
    class PesquisaTests {

        @Test
        @DisplayName("Deve pesquisar incluindo inativos")
        void devePesquisarIncluindoInativos() {
            String termo = "123";
            List<ProfissionalResumoDTO> lista = List.of(resumoDTO);
            doReturn(lista).when(profissionalService).buscarPorMultiplosCampos(termo);

            ResponseEntity<List<ProfissionalResumoDTO>> response = profissionalController.pesquisar(termo, false);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(profissionalService).buscarPorMultiplosCampos(termo);
        }

        @Test
        @DisplayName("Deve pesquisar apenas ativos")
        void devePesquisarApenasAtivos() {
            String termo = "123";
            List<ProfissionalResumoDTO> lista = List.of(resumoDTO);
            doReturn(lista).when(profissionalService).buscarAtivosPorMultiplosCampos(termo);

            ResponseEntity<List<ProfissionalResumoDTO>> response = profissionalController.pesquisar(termo, true);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(profissionalService).buscarAtivosPorMultiplosCampos(termo);
        }
    }

    @Nested
    @DisplayName("Testes de Paginação")
    class PaginacaoTests {

        @Test
        @DisplayName("Deve listar com paginação")
        void deveListarComPaginacao() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<ProfissionalResumoDTO> page = new PageImpl<>(List.of(resumoDTO), pageable, 1);
            when(profissionalService.listarComPaginacao(pageable)).thenReturn(page);

            ResponseEntity<Page<ProfissionalResumoDTO>> response = profissionalController.listarComPaginacao(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().getTotalElements());
        }
    }

    @Nested
    @DisplayName("Testes de Status e Deleção")
    class StatusDelecaoTests {

        @Test
        @DisplayName("Deve alternar status com sucesso")
        void deveAlternarStatusComSucesso() {
            String uuid = "uuid-123";
            doNothing().when(profissionalService).toggleStatus(uuid, usuarioLogado);

            ResponseEntity<Void> response = profissionalController.toggleStatus(uuid, usuarioLogado);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(profissionalService).toggleStatus(uuid, usuarioLogado);
        }

        @Test
        @DisplayName("Deve deletar profissional com sucesso")
        void deveDeletarProfissionalComSucesso() {
            String uuid = "uuid-123";
            doNothing().when(profissionalService).deletar(uuid);

            ResponseEntity<Void> response = profissionalController.deletar(uuid);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(profissionalService).deletar(uuid);
        }
    }

    @Nested
    @DisplayName("Testes de Tipos de Vínculo")
    class TiposVinculoTests {

        @Test
        @DisplayName("Deve listar tipos de vínculo")
        void deveListarTiposVinculo() {
            TipoVinculoEntity tipoEntity = new TipoVinculoEntity();
            tipoEntity.setId(1L);
            tipoEntity.setCodigo("CLT");
            tipoEntity.setNome("Contratado CLT");
            tipoEntity.setAtivo(true);
            
            List<TipoVinculoEntity> tipos = List.of(tipoEntity);
            when(tipoVinculoRepository.findByAtivoTrueOrderByNomeAsc()).thenReturn(tipos);

            ResponseEntity<List<TipoVinculoDTO>> response = profissionalController.listarTiposVinculo();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals("CLT", response.getBody().get(0).getCodigo());
            verify(tipoVinculoRepository).findByAtivoTrueOrderByNomeAsc();
        }
    }

    @Nested
    @DisplayName("Testes de Categorias")
    class CategoriasTests {

        @Test
        @DisplayName("Deve listar categorias profissionais")
        void deveListarCategorias() {
            ResponseEntity<List<CategoriaProfissionalDTO>> response = profissionalController.listarCategorias();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("Testes de Dashboard Stats")
    class DashboardStatsTests {

        @Test
        @DisplayName("Deve retornar estatísticas do dashboard")
        void deveRetornarEstatisticasDashboard() {
            DashboardStatsDTO stats = DashboardStatsDTO.builder()
                    .totalProfissionaisAtivos(10L)
                    .totalProfissionaisInativos(2L)
                    .admitidosUltimos30Dias(3L)
                    .comEnderecoCadastrado(8L)
                    .semEnderecoCadastrado(4L)
                    .build();
            when(profissionalService.getDashboardStats()).thenReturn(stats);

            ResponseEntity<DashboardStatsDTO> response = profissionalController.getDashboardStats();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(10L, response.getBody().getTotalProfissionaisAtivos());
            assertEquals(2L, response.getBody().getTotalProfissionaisInativos());
        }
    }
}
