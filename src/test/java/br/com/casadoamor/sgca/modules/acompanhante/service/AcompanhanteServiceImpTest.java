package br.com.casadoamor.sgca.modules.acompanhante.service;

import br.com.casadoamor.sgca.infra.config.exception.CustomError;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.AcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.EditarAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.dtos.RegistrarAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.acompanhante.entity.Acompanhante;
import br.com.casadoamor.sgca.modules.acompanhante.mapper.AcompanhanteMapper;
import br.com.casadoamor.sgca.modules.acompanhante.repository.AcompanhanteRepository;
import br.com.casadoamor.sgca.modules.common.dto.PaginatedResponseDTO;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.common.enums.EstadoEnum;
import br.com.casadoamor.sgca.modules.common.enums.Parentesco;
import br.com.casadoamor.sgca.modules.common.enums.SexoEnum;
import br.com.casadoamor.sgca.modules.common.mapper.DadoPessoalMapper;
import br.com.casadoamor.sgca.modules.common.mapper.EnderecoMapper;
import br.com.casadoamor.sgca.modules.common.mapper.PaginatedResponseMapper;
import br.com.casadoamor.sgca.modules.common.repository.DadoPessoalRepository;
import br.com.casadoamor.sgca.modules.paciente.dtos.DadoPessoalInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarDadoPessoalInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.EditarEnderecoInputDTO;
import br.com.casadoamor.sgca.modules.paciente.dtos.HistoricoAcompanhanteDTO;
import br.com.casadoamor.sgca.modules.paciente.entity.HistoricoPaciente;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import br.com.casadoamor.sgca.modules.paciente.mapper.HistoricoPacienteMapper;
import br.com.casadoamor.sgca.modules.paciente.repository.HistoricoPacienteRepository;
import br.com.casadoamor.sgca.modules.paciente.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcompanhanteServiceImpTest {

    @Mock
    private AcompanhanteRepository acompanhanteRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private HistoricoPacienteRepository historicoRepository;

    @Mock
    private AcompanhanteMapper acompanhanteMapper;

    @Mock
    private HistoricoPacienteMapper historicoPacienteMapper;

    @Mock
    private DadoPessoalRepository dadoPessoalRepository;

    @Mock
    private DadoPessoalMapper dadoPessoalMapper;

    @Mock
    private EnderecoMapper enderecoMapper;

    @Mock
    private PaginatedResponseMapper paginatedMapper;

    @InjectMocks
    private AcompanhanteServiceImp acompanhanteService;

    private Paciente paciente;
    private Acompanhante acompanhante;
    private DadoPessoal dadoPessoal;
    private Endereco endereco;
    private AcompanhanteDTO acompanhanteDTO;

    @BeforeEach
    void setUp() {
        dadoPessoal = DadoPessoal.builder()
                .nome("Maria Silva")
                .cpf("11122233344")
                .rg("123456789")
                .dataNascimento(LocalDate.of(1980, 1, 1))
                .sexo(SexoEnum.FEMININO)
                .build();

        endereco = Endereco.builder()
                .logradouro("Rua Teste")
                .bairro("Centro")
                .numero(100)
                .cidade("São Paulo")
                .estado(EstadoEnum.SAO_PAULO)
                .cep("01234567")
                .build();

        paciente = new Paciente();
        paciente.setId("paciente-uuid-123");
        paciente.setDadoPessoal(dadoPessoal);
        paciente.setEndereco(endereco);

        acompanhante = new Acompanhante();
        acompanhante.setId("acompanhante-uuid-123");
        acompanhante.setDadoPessoal(dadoPessoal);
        acompanhante.setEndereco(endereco);
        acompanhante.setPaciente(paciente);
        acompanhante.setParentesco(Parentesco.MAE);
        acompanhante.setAtivo(true);
        acompanhante.setPodeAjudarNaCozinha(false);

        acompanhanteDTO = AcompanhanteDTO.builder()
                .id("acompanhante-uuid-123")
                .parentesco(Parentesco.MAE)
                .ativo(true)
                .build();
    }

    @Nested
    @DisplayName("Testes de Registro")
    class RegistrarTests {

        @Test
        @DisplayName("Deve registrar acompanhante com sucesso")
        void deveRegistrarAcompanhanteComSucesso() {
            DadoPessoalInputDTO dadoPessoalInputDTO = DadoPessoalInputDTO.builder()
                    .nome("Maria Silva")
                    .cpf("11122233344")
                    .rg("123456789")
                    .dataNascimento(LocalDate.of(1980, 1, 1))
                    .naturalidade("São Paulo")
                    .telefone("11999998888")
                    .sexo(SexoEnum.FEMININO)
                    .build();

            RegistrarAcompanhanteDTO dto = new RegistrarAcompanhanteDTO();
            dto.setPacienteId("paciente-uuid-123");
            dto.setDadoPessoal(dadoPessoalInputDTO);
            dto.setParentesco(Parentesco.MAE);
            dto.setPodeAjudarNaCozinha(false);

            when(pacienteRepository.findById("paciente-uuid-123")).thenReturn(Optional.of(paciente));
            when(dadoPessoalRepository.findByCpf("11122233344")).thenReturn(Optional.empty());
            when(dadoPessoalRepository.findByRg("123456789")).thenReturn(Optional.empty());
            when(acompanhanteMapper.toEntity(dto, paciente)).thenReturn(acompanhante);
            when(acompanhanteRepository.save(acompanhante)).thenReturn(acompanhante);
            when(historicoPacienteMapper.toEntity(any(), any(), anyString())).thenReturn(new HistoricoPaciente());
            when(acompanhanteMapper.mapToDTO(acompanhante)).thenReturn(acompanhanteDTO);

            AcompanhanteDTO result = acompanhanteService.registrarAcompanhante(dto);

            assertNotNull(result);
            verify(acompanhanteRepository).save(acompanhante);
            verify(historicoRepository).save(any(HistoricoPaciente.class));
        }

        @Test
        @DisplayName("Deve lançar erro quando paciente não encontrado")
        void deveLancarErroQuandoPacienteNaoEncontrado() {
            DadoPessoalInputDTO dadoPessoalInputDTO = DadoPessoalInputDTO.builder()
                    .cpf("11122233344")
                    .build();

            RegistrarAcompanhanteDTO dto = new RegistrarAcompanhanteDTO();
            dto.setPacienteId("nao-existe");
            dto.setDadoPessoal(dadoPessoalInputDTO);

            when(pacienteRepository.findById("nao-existe")).thenReturn(Optional.empty());

            assertThrows(CustomError.class, () -> acompanhanteService.registrarAcompanhante(dto));
        }

        @Test
        @DisplayName("Deve lançar erro quando CPF já cadastrado")
        void deveLancarErroQuandoCpfJaCadastrado() {
            DadoPessoalInputDTO dadoPessoalInputDTO = DadoPessoalInputDTO.builder()
                    .cpf("11122233344")
                    .rg("123456789")
                    .build();

            RegistrarAcompanhanteDTO dto = new RegistrarAcompanhanteDTO();
            dto.setPacienteId("paciente-uuid-123");
            dto.setDadoPessoal(dadoPessoalInputDTO);

            when(pacienteRepository.findById("paciente-uuid-123")).thenReturn(Optional.of(paciente));
            when(dadoPessoalRepository.findByCpf("11122233344")).thenReturn(Optional.of(dadoPessoal));

            assertThrows(CustomError.class, () -> acompanhanteService.registrarAcompanhante(dto));
        }

        @Test
        @DisplayName("Deve lançar erro quando RG já cadastrado")
        void deveLancarErroQuandoRgJaCadastrado() {
            DadoPessoalInputDTO dadoPessoalInputDTO = DadoPessoalInputDTO.builder()
                    .cpf("11122233344")
                    .rg("123456789")
                    .build();

            RegistrarAcompanhanteDTO dto = new RegistrarAcompanhanteDTO();
            dto.setPacienteId("paciente-uuid-123");
            dto.setDadoPessoal(dadoPessoalInputDTO);

            when(pacienteRepository.findById("paciente-uuid-123")).thenReturn(Optional.of(paciente));
            when(dadoPessoalRepository.findByCpf("11122233344")).thenReturn(Optional.empty());
            when(dadoPessoalRepository.findByRg("123456789")).thenReturn(Optional.of(dadoPessoal));

            assertThrows(CustomError.class, () -> acompanhanteService.registrarAcompanhante(dto));
        }
    }

    @Nested
    @DisplayName("Testes de Edição")
    class EditarTests {

        @Test
        @DisplayName("Deve editar acompanhante com sucesso - todos os campos")
        void deveEditarAcompanhanteComSucessoTodosCampos() {
            EditarDadoPessoalInputDTO editarDadoPessoal = EditarDadoPessoalInputDTO.builder()
                    .nome("Maria Santos")
                    .build();

            EditarEnderecoInputDTO editarEndereco = EditarEnderecoInputDTO.builder()
                    .cidade("Rio de Janeiro")
                    .build();

            EditarAcompanhanteDTO dto = new EditarAcompanhanteDTO();
            dto.setDadoPessoal(editarDadoPessoal);
            dto.setEndereco(editarEndereco);
            dto.setParentesco(Parentesco.AVÓ);
            dto.setAtivo(true);
            dto.setPodeAjudarNaCozinha(true);

            when(acompanhanteRepository.findById("acompanhante-uuid-123")).thenReturn(Optional.of(acompanhante));
            when(dadoPessoalMapper.updateEntity(any(), any())).thenReturn(dadoPessoal);
            when(enderecoMapper.updateEntity(any(), any())).thenReturn(endereco);
            when(acompanhanteRepository.save(acompanhante)).thenReturn(acompanhante);
            when(historicoPacienteMapper.toEntity(any(), any(), anyString())).thenReturn(new HistoricoPaciente());
            when(acompanhanteMapper.mapToDTO(acompanhante)).thenReturn(acompanhanteDTO);

            AcompanhanteDTO result = acompanhanteService.editarAcompanhante("acompanhante-uuid-123", dto);

            assertNotNull(result);
            verify(acompanhanteRepository).save(acompanhante);
            verify(historicoRepository).save(any(HistoricoPaciente.class));
        }

        @Test
        @DisplayName("Deve editar acompanhante apenas podeAjudarNaCozinha")
        void deveEditarApenasPodeAjudarNaCozinha() {
            EditarAcompanhanteDTO dto = new EditarAcompanhanteDTO();
            dto.setPodeAjudarNaCozinha(true);

            when(acompanhanteRepository.findById("acompanhante-uuid-123")).thenReturn(Optional.of(acompanhante));
            when(acompanhanteRepository.save(acompanhante)).thenReturn(acompanhante);
            when(historicoPacienteMapper.toEntity(any(), any(), anyString())).thenReturn(new HistoricoPaciente());
            when(acompanhanteMapper.mapToDTO(acompanhante)).thenReturn(acompanhanteDTO);

            AcompanhanteDTO result = acompanhanteService.editarAcompanhante("acompanhante-uuid-123", dto);

            assertNotNull(result);
            assertTrue(acompanhante.getPodeAjudarNaCozinha());
        }

        @Test
        @DisplayName("Deve lançar erro quando acompanhante não encontrado na edição")
        void deveLancarErroQuandoAcompanhanteNaoEncontradoNaEdicao() {
            EditarAcompanhanteDTO dto = new EditarAcompanhanteDTO();

            when(acompanhanteRepository.findById("nao-existe")).thenReturn(Optional.empty());

            assertThrows(CustomError.class, () -> acompanhanteService.editarAcompanhante("nao-existe", dto));
        }

        @Test
        @DisplayName("Deve lançar erro quando CPF já cadastrado em outro registro")
        void deveLancarErroQuandoCpfJaCadastradoEmOutroRegistro() {
            EditarDadoPessoalInputDTO editarDadoPessoal = EditarDadoPessoalInputDTO.builder()
                    .cpf("55566677788")
                    .build();

            EditarAcompanhanteDTO dto = new EditarAcompanhanteDTO();
            dto.setDadoPessoal(editarDadoPessoal);

            when(acompanhanteRepository.findById("acompanhante-uuid-123")).thenReturn(Optional.of(acompanhante));
            when(pacienteRepository.existsByCpf("55566677788")).thenReturn(true);

            assertThrows(CustomError.class, () -> acompanhanteService.editarAcompanhante("acompanhante-uuid-123", dto));
        }

        @Test
        @DisplayName("Deve lançar erro quando RG já cadastrado em outro registro")
        void deveLancarErroQuandoRgJaCadastradoEmOutroRegistro() {
            EditarDadoPessoalInputDTO editarDadoPessoal = EditarDadoPessoalInputDTO.builder()
                    .rg("998887776")
                    .build();

            EditarAcompanhanteDTO dto = new EditarAcompanhanteDTO();
            dto.setDadoPessoal(editarDadoPessoal);

            when(acompanhanteRepository.findById("acompanhante-uuid-123")).thenReturn(Optional.of(acompanhante));
            when(pacienteRepository.existsByRg("998887776")).thenReturn(true);

            assertThrows(CustomError.class, () -> acompanhanteService.editarAcompanhante("acompanhante-uuid-123", dto));
        }
    }

    @Nested
    @DisplayName("Testes de Paginação")
    class PaginacaoTests {

        @Test
        @DisplayName("Deve listar acompanhantes paginados sem busca")
        @SuppressWarnings("unchecked")
        void deveListarAcompanhantesPaginadosSemBusca() {
            List<Acompanhante> acompanhantes = List.of(acompanhante);
            PaginatedResponseDTO<AcompanhanteDTO> expectedResponse = PaginatedResponseDTO.<AcompanhanteDTO>builder()
                    .nodes(List.of(acompanhanteDTO))
                    .totalCount(1)
                    .hasNextPage(false)
                    .hasPreviousPage(false)
                    .build();

            doReturn(acompanhantes).when(acompanhanteRepository).findAll(any(Specification.class));
            when(acompanhanteMapper.mapToDTO(acompanhante)).thenReturn(acompanhanteDTO);
            doReturn(expectedResponse).when(paginatedMapper).toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean());

            PaginatedResponseDTO<AcompanhanteDTO> result = acompanhanteService.acompanhantesPaginados(null, 10, 0);

            assertNotNull(result);
            verify(acompanhanteRepository).findAll(any(Specification.class));
        }

        @Test
        @DisplayName("Deve listar acompanhantes paginados com busca")
        @SuppressWarnings("unchecked")
        void deveListarAcompanhantesPaginadosComBusca() {
            List<Acompanhante> acompanhantes = List.of(acompanhante);
            PaginatedResponseDTO<AcompanhanteDTO> expectedResponse = PaginatedResponseDTO.<AcompanhanteDTO>builder()
                    .nodes(List.of(acompanhanteDTO))
                    .totalCount(1)
                    .hasNextPage(false)
                    .hasPreviousPage(false)
                    .build();

            doReturn(acompanhantes).when(acompanhanteRepository).findAll(any(Specification.class));
            when(acompanhanteMapper.mapToDTO(acompanhante)).thenReturn(acompanhanteDTO);
            doReturn(expectedResponse).when(paginatedMapper).toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean());

            PaginatedResponseDTO<AcompanhanteDTO> result = acompanhanteService.acompanhantesPaginados("Maria", 10, 0);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há acompanhantes")
        @SuppressWarnings("unchecked")
        void deveRetornarListaVaziaQuandoNaoHaAcompanhantes() {
            List<Acompanhante> acompanhantes = new ArrayList<>();
            PaginatedResponseDTO<AcompanhanteDTO> expectedResponse = PaginatedResponseDTO.<AcompanhanteDTO>builder()
                    .nodes(List.of())
                    .totalCount(0)
                    .hasNextPage(false)
                    .hasPreviousPage(false)
                    .build();

            doReturn(acompanhantes).when(acompanhanteRepository).findAll(any(Specification.class));
            doReturn(expectedResponse).when(paginatedMapper).toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean());

            PaginatedResponseDTO<AcompanhanteDTO> result = acompanhanteService.acompanhantesPaginados(null, 10, 0);

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Testes de Histórico")
    class HistoricoTests {

        @Test
        @DisplayName("Deve listar histórico de acompanhante paginado")
        void deveListarHistoricoAcompanhantePaginado() {
            List<HistoricoPaciente> historicos = List.of(new HistoricoPaciente());
            HistoricoAcompanhanteDTO historicoDTO = new HistoricoAcompanhanteDTO();
            PaginatedResponseDTO<HistoricoAcompanhanteDTO> expectedResponse = PaginatedResponseDTO.<HistoricoAcompanhanteDTO>builder()
                    .nodes(List.of(historicoDTO))
                    .totalCount(1)
                    .hasNextPage(false)
                    .hasPreviousPage(false)
                    .build();

            when(acompanhanteRepository.findById("acompanhante-uuid-123")).thenReturn(Optional.of(acompanhante));
            doReturn(historicos).when(historicoRepository).findByAcompanhanteOrderByDataRegistroDesc(acompanhante);
            when(historicoPacienteMapper.toHistoricoAcompanhanteDTO(any())).thenReturn(historicoDTO);
            doReturn(expectedResponse).when(paginatedMapper).toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean());

            PaginatedResponseDTO<HistoricoAcompanhanteDTO> result = 
                    acompanhanteService.historicoAcompanhantePaginado("acompanhante-uuid-123", 10, 0);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Deve lançar erro quando acompanhante não encontrado no histórico")
        void deveLancarErroQuandoAcompanhanteNaoEncontradoNoHistorico() {
            when(acompanhanteRepository.findById("nao-existe")).thenReturn(Optional.empty());

            assertThrows(CustomError.class, 
                    () -> acompanhanteService.historicoAcompanhantePaginado("nao-existe", 10, 0));
        }
    }

    @Nested
    @DisplayName("Testes de Deleção")
    class DelecaoTests {

        @Test
        @DisplayName("Deve deletar acompanhante com sucesso")
        void deveDeletarAcompanhanteComSucesso() {
            // Mock do SecurityContext
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("admin@test.com");
            SecurityContextHolder.setContext(securityContext);

            when(acompanhanteRepository.findById("acompanhante-uuid-123")).thenReturn(Optional.of(acompanhante));
            when(acompanhanteRepository.save(any(Acompanhante.class))).thenReturn(acompanhante);

            assertDoesNotThrow(() -> acompanhanteService.deletarAcompanhante("acompanhante-uuid-123"));
            
            verify(acompanhanteRepository).save(acompanhante);
            assertFalse(acompanhante.isAtivo());
        }

        @Test
        @DisplayName("Deve lançar erro quando acompanhante não encontrado na deleção")
        void deveLancarErroQuandoAcompanhanteNaoEncontradoNaDelecao() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("admin@test.com");
            SecurityContextHolder.setContext(securityContext);

            when(acompanhanteRepository.findById("nao-existe")).thenReturn(Optional.empty());

            assertThrows(CustomError.class, () -> acompanhanteService.deletarAcompanhante("nao-existe"));
        }

        @Test
        @DisplayName("Deve lançar erro quando acompanhante já deletado")
        void deveLancarErroQuandoAcompanhanteJaDeletado() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("admin@test.com");
            SecurityContextHolder.setContext(securityContext);

            acompanhante.markAsDeleted("outro-admin");
            when(acompanhanteRepository.findById("acompanhante-uuid-123")).thenReturn(Optional.of(acompanhante));

            assertThrows(CustomError.class, () -> acompanhanteService.deletarAcompanhante("acompanhante-uuid-123"));
        }
    }

    @Nested
    @DisplayName("Testes de Listagem por Paciente")
    class ListagemPorPacienteTests {

        @Test
        @DisplayName("Deve listar acompanhantes por paciente sem busca")
        @SuppressWarnings("unchecked")
        void deveListarAcompanhantesPorPacienteSemBusca() {
            List<Acompanhante> acompanhantes = List.of(acompanhante);
            PaginatedResponseDTO<AcompanhanteDTO> expectedResponse = PaginatedResponseDTO.<AcompanhanteDTO>builder()
                    .nodes(List.of(acompanhanteDTO))
                    .totalCount(1)
                    .hasNextPage(false)
                    .hasPreviousPage(false)
                    .build();

            when(pacienteRepository.findById("paciente-uuid-123")).thenReturn(Optional.of(paciente));
            doReturn(acompanhantes).when(acompanhanteRepository).findAll(any(Specification.class));
            when(acompanhanteMapper.mapToDTO(acompanhante)).thenReturn(acompanhanteDTO);
            doReturn(expectedResponse).when(paginatedMapper).toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean());

            PaginatedResponseDTO<AcompanhanteDTO> result = 
                    acompanhanteService.listarAcompanhantesPorPaciente(null, "paciente-uuid-123", 10, 0);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Deve listar acompanhantes por paciente com busca")
        @SuppressWarnings("unchecked")
        void deveListarAcompanhantesPorPacienteComBusca() {
            List<Acompanhante> acompanhantes = List.of(acompanhante);
            PaginatedResponseDTO<AcompanhanteDTO> expectedResponse = PaginatedResponseDTO.<AcompanhanteDTO>builder()
                    .nodes(List.of(acompanhanteDTO))
                    .totalCount(1)
                    .hasNextPage(false)
                    .hasPreviousPage(false)
                    .build();

            when(pacienteRepository.findById("paciente-uuid-123")).thenReturn(Optional.of(paciente));
            doReturn(acompanhantes).when(acompanhanteRepository).findAll(any(Specification.class));
            when(acompanhanteMapper.mapToDTO(acompanhante)).thenReturn(acompanhanteDTO);
            doReturn(expectedResponse).when(paginatedMapper).toDTO(anyList(), anyLong(), anyBoolean(), anyBoolean());

            PaginatedResponseDTO<AcompanhanteDTO> result = 
                    acompanhanteService.listarAcompanhantesPorPaciente("Maria", "paciente-uuid-123", 10, 0);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Deve lançar erro quando paciente não encontrado")
        void deveLancarErroQuandoPacienteNaoEncontrado() {
            when(pacienteRepository.findById("nao-existe")).thenReturn(Optional.empty());

            assertThrows(CustomError.class, 
                    () -> acompanhanteService.listarAcompanhantesPorPaciente(null, "nao-existe", 10, 0));
        }
    }
}
