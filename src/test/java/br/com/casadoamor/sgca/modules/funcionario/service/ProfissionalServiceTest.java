package br.com.casadoamor.sgca.modules.funcionario.service;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.common.enums.EstadoEnum;
import br.com.casadoamor.sgca.modules.common.entity.Endereco;
import br.com.casadoamor.sgca.modules.common.repository.EnderecoRepository;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalRequestDTO;
import br.com.casadoamor.sgca.modules.funcionario.dto.ProfissionalResponseDTO;
import br.com.casadoamor.sgca.modules.funcionario.entity.Profissional;
import br.com.casadoamor.sgca.modules.funcionario.entity.enums.CategoriaProfissional;
import br.com.casadoamor.sgca.modules.funcionario.repository.ProfissionalRepository;
import br.com.casadoamor.sgca.modules.funcionario.repository.TipoVinculoRepository;
import br.com.casadoamor.sgca.modules.paciente.dtos.EnderecoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTest {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private TipoVinculoRepository tipoVinculoRepository;

    @InjectMocks
    private ProfissionalService profissionalService;

    private ProfissionalRequestDTO requestDTO;
    private AuthUsuario usuarioLogado;
    private Profissional profissional;

    @BeforeEach
    void setUp() {
        usuarioLogado = AuthUsuario.builder().id(1L).nome("Admin").build();

        EnderecoDTO enderecoDTO = EnderecoDTO.builder()
                .logradouro("Rua Teste")
                .numero(123)
                .bairro("Centro")
                .cidade("Cidade")
                .estado(EstadoEnum.SAO_PAULO)
                .cep("12345-678")
                .build();

        requestDTO = ProfissionalRequestDTO.builder()
                .nome("Profissional Teste")
                .cpf("123.456.789-00")
                .categoria(CategoriaProfissional.MEDICO)
                .endereco(enderecoDTO)
                .build();

        profissional = Profissional.builder()
                .uuid("uuid-teste")
                .nome("Profissional Teste")
                .cpf("123.456.789-00")
                .categoria(CategoriaProfissional.MEDICO)
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve cadastrar profissional com sucesso")
    void deveCadastrarProfissionalComSucesso() {
        when(profissionalRepository.findByCpfCriptografado(any())).thenReturn(Optional.empty());
        when(enderecoRepository.save(any(Endereco.class))).thenAnswer(i -> i.getArgument(0));
        when(profissionalRepository.save(any(Profissional.class))).thenAnswer(i -> {
            Profissional p = i.getArgument(0);
            p.setUuid("uuid-gerado");
            return p;
        });

        ProfissionalResponseDTO response = profissionalService.cadastrar(requestDTO, usuarioLogado);

        assertThat(response).isNotNull();
        assertThat(response.getNome()).isEqualTo(requestDTO.getNome());
        verify(profissionalRepository).save(any(Profissional.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar CPF já existente")
    void deveLancarExcecaoCpfExistente() {
        when(profissionalRepository.findByCpfCriptografado(any())).thenReturn(Optional.of(profissional));

        assertThatThrownBy(() -> profissionalService.cadastrar(requestDTO, usuarioLogado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF já cadastrado");
    }

    @Test
    @DisplayName("Deve atualizar profissional com sucesso")
    void deveAtualizarProfissionalComSucesso() {
        when(profissionalRepository.findByUuid("uuid-teste")).thenReturn(Optional.of(profissional));
        when(profissionalRepository.save(any(Profissional.class))).thenReturn(profissional);

        ProfissionalRequestDTO updateDTO = ProfissionalRequestDTO.builder()
                .nome("Nome Atualizado")
                .build();

        ProfissionalResponseDTO response = profissionalService.atualizar("uuid-teste", updateDTO, usuarioLogado);

        assertThat(response.getNome()).isEqualTo("Nome Atualizado");
        verify(profissionalRepository).save(any(Profissional.class));
    }

    @Test
    @DisplayName("Deve buscar profissional por UUID")
    void deveBuscarPorUuid() {
        when(profissionalRepository.findByUuid("uuid-teste")).thenReturn(Optional.of(profissional));

        ProfissionalResponseDTO response = profissionalService.buscarPorUuid("uuid-teste");

        assertThat(response).isNotNull();
        assertThat(response.getUuid()).isEqualTo("uuid-teste");
    }

    @Test
    @DisplayName("Deve listar todos os profissionais")
    void deveListarTodos() {
        when(profissionalRepository.findAll()).thenReturn(List.of(profissional));

        var result = profissionalService.listarTodos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo(profissional.getNome());
    }

    @Test
    @DisplayName("Deve listar profissionais por categoria")
    void deveListarPorCategoria() {
        when(profissionalRepository.findByCategoria(CategoriaProfissional.MEDICO)).thenReturn(List.of(profissional));

        var result = profissionalService.listarPorCategoria(CategoriaProfissional.MEDICO);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoria()).isNotNull();
    }

    @Test
    @DisplayName("Deve listar profissionais ativos")
    void deveListarAtivos() {
        when(profissionalRepository.findByAtivoTrue()).thenReturn(List.of(profissional));

        var result = profissionalService.listarAtivos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve buscar profissionais por nome")
    void deveBuscarPorNome() {
        when(profissionalRepository.searchByNome("Teste")).thenReturn(List.of(profissional));

        var result = profissionalService.buscarPorNome("Teste");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo(profissional.getNome());
    }

    @Test
    @DisplayName("Deve buscar por múltiplos campos")
    void deveBuscarPorMultiplosCampos() {
        when(profissionalRepository.searchByMultipleFields("termo")).thenReturn(List.of(profissional));

        var result = profissionalService.buscarPorMultiplosCampos("termo");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar ativos por múltiplos campos")
    void deveBuscarAtivosPorMultiplosCampos() {
        when(profissionalRepository.searchActiveByMultipleFields("termo")).thenReturn(List.of(profissional));

        var result = profissionalService.buscarAtivosPorMultiplosCampos("termo");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve listar com paginação")
    void deveListarComPaginacao() {
        Pageable pageable = Pageable.unpaged();
        Page<Profissional> page = new PageImpl<>(List.of(profissional));
        when(profissionalRepository.findAll(pageable)).thenReturn(page);

        var result = profissionalService.listarComPaginacao(pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve deletar profissional")
    void deveDeletar() {
        when(profissionalRepository.findByUuid("uuid-teste")).thenReturn(Optional.of(profissional));

        profissionalService.deletar("uuid-teste");

        verify(profissionalRepository).delete(profissional);
    }

    @Test
    @DisplayName("Deve alternar status do profissional")
    void deveAlternarStatus() {
        when(profissionalRepository.findByUuid("uuid-teste")).thenReturn(Optional.of(profissional));

        profissionalService.toggleStatus("uuid-teste", usuarioLogado);

        assertThat(profissional.getAtivo()).isFalse();
        verify(profissionalRepository).save(profissional);
    }

    // ========== New Tests - Validation & Error Handling ==========

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar com número de registro duplicado")
    void deveLancarExcecaoAoCadastrarComNumeroRegistroDuplicado() {
        ProfissionalRequestDTO dtoComRegistro = ProfissionalRequestDTO.builder()
                .nome("Profissional Teste")
                .cpf("987.654.321-00")
                .categoria(CategoriaProfissional.MEDICO)
                .numeroRegistro("CRM123456")
                .ufRegistro("SP")
                .build();

        when(profissionalRepository.findByCpfCriptografado(any())).thenReturn(Optional.empty());
        when(profissionalRepository.existsByNumeroRegistroAndUfRegistro("CRM123456", "SP")).thenReturn(true);

        assertThatThrownBy(() -> profissionalService.cadastrar(dtoComRegistro, usuarioLogado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número de registro profissional já cadastrado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar por UUID inexistente")
    void deveLancarExcecaoAoBuscarPorUuidInexistente() {
        when(profissionalRepository.findByUuid("uuid-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profissionalService.buscarPorUuid("uuid-inexistente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Profissional não encontrado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar profissional inexistente")
    void deveLancarExcecaoAoAtualizarProfissionalInexistente() {
        when(profissionalRepository.findByUuid("uuid-inexistente")).thenReturn(Optional.empty());

        ProfissionalRequestDTO updateDTO = ProfissionalRequestDTO.builder()
                .nome("Nome Atualizado")
                .build();

        assertThatThrownBy(() -> profissionalService.atualizar("uuid-inexistente", updateDTO, usuarioLogado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Profissional não encontrado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar profissional inexistente")
    void deveLancarExcecaoAoDeletarProfissionalInexistente() {
        when(profissionalRepository.findByUuid("uuid-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profissionalService.deletar("uuid-inexistente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Profissional não encontrado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao toggle status de profissional inexistente")
    void deveLancarExcecaoAoToggleStatusProfissionalInexistente() {
        when(profissionalRepository.findByUuid("uuid-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profissionalService.toggleStatus("uuid-inexistente", usuarioLogado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Profissional não encontrado");
    }

    // ========== New Tests - Endereco Management ==========

    @Test
    @DisplayName("Deve cadastrar profissional com endereço existente")
    void deveCadastrarProfissionalComEnderecoExistente() {
        Endereco enderecoExistente = Endereco.builder()
                .logradouro("Rua Existente")
                .bairro("Bairro Teste")
                .cidade("Cidade Teste")
                .estado(EstadoEnum.SAO_PAULO)
                .build();
        enderecoExistente.setId("uuid-endereco-10");

        ProfissionalRequestDTO dtoComEnderecoId = ProfissionalRequestDTO.builder()
                .nome("Profissional Teste")
                .cpf("111.222.333-44")
                .categoria(CategoriaProfissional.ENFERMAGEM)
                .enderecoId("uuid-endereco-10")
                .build();

        when(profissionalRepository.findByCpfCriptografado(any())).thenReturn(Optional.empty());
        when(enderecoRepository.findById("uuid-endereco-10")).thenReturn(Optional.of(enderecoExistente));
        when(profissionalRepository.save(any(Profissional.class))).thenAnswer(i -> {
            Profissional p = i.getArgument(0);
            p.setUuid("uuid-novo");
            return p;
        });

        ProfissionalResponseDTO response = profissionalService.cadastrar(dtoComEnderecoId, usuarioLogado);

        assertThat(response).isNotNull();
        verify(enderecoRepository).findById("uuid-endereco-10");
        verify(enderecoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar endereço inline")
    void deveAtualizarEnderecoInline() {
        Endereco enderecoAtual = Endereco.builder()
                .logradouro("Rua Antiga")
                .bairro("Bairro Antigo")
                .cidade("Cidade Antiga")
                .estado(EstadoEnum.SAO_PAULO)
                .build();
        enderecoAtual.setId("uuid-endereco-5");
        profissional.setEndereco(enderecoAtual);

        EnderecoDTO novoEndereco = EnderecoDTO.builder()
                .logradouro("Rua Nova")
                .numero(456)
                .bairro("Bairro Novo")
                .cidade("Cidade Nova")
                .estado(EstadoEnum.RIO_DE_JANEIRO)
                .cep("98765-432")
                .build();

        ProfissionalRequestDTO updateDTO = ProfissionalRequestDTO.builder()
                .endereco(novoEndereco)
                .build();

        when(profissionalRepository.findByUuid("uuid-teste")).thenReturn(Optional.of(profissional));
        when(enderecoRepository.save(any(Endereco.class))).thenAnswer(i -> i.getArgument(0));
        when(profissionalRepository.save(any(Profissional.class))).thenReturn(profissional);

        profissionalService.atualizar("uuid-teste", updateDTO, usuarioLogado);

        verify(enderecoRepository).save(any(Endereco.class));
        verify(profissionalRepository).save(profissional);
    }

    @Test
    @DisplayName("Deve atualizar com endereço existente")
    void deveAtualizarComEnderecoExistente() {
        Endereco novoEndereco = Endereco.builder()
                .logradouro("Rua Referenciada")
                .bairro("Bairro Referenciado")
                .cidade("Cidade Referenciada")
                .estado(EstadoEnum.SAO_PAULO)
                .build();
        novoEndereco.setId("uuid-endereco-20");

        ProfissionalRequestDTO updateDTO = ProfissionalRequestDTO.builder()
                .enderecoId("uuid-endereco-20")
                .build();

        when(profissionalRepository.findByUuid("uuid-teste")).thenReturn(Optional.of(profissional));
        when(enderecoRepository.findById("uuid-endereco-20")).thenReturn(Optional.of(novoEndereco));
        when(profissionalRepository.save(any(Profissional.class))).thenReturn(profissional);

        profissionalService.atualizar("uuid-teste", updateDTO, usuarioLogado);

        assertThat(profissional.getEndereco()).isEqualTo(novoEndereco);
        verify(enderecoRepository).findById("uuid-endereco-20");
    }

    // ========== New Tests - Dashboard & Statistics ==========

    @Test
    @DisplayName("Deve obter estatísticas do dashboard")
    void deveObterEstatisticasDashboard() {
        Profissional prof1 = Profissional.builder()
                .uuid("uuid-1")
                .nome("Prof 1")
                .categoria(CategoriaProfissional.MEDICO)
                .areaAtuacao("Cardiologia")
                .ativo(true)
                .build();

        Profissional prof2 = Profissional.builder()
                .uuid("uuid-2")
                .nome("Prof 2")
                .categoria(CategoriaProfissional.ENFERMAGEM)
                .areaAtuacao("Pediatria")
                .ativo(true)
                .build();

        Profissional prof3 = Profissional.builder()
                .uuid("uuid-3")
                .nome("Prof 3")
                .categoria(CategoriaProfissional.MEDICO)
                .ativo(false)
                .build();

        when(profissionalRepository.countByAtivo(true)).thenReturn(2L);
        when(profissionalRepository.countByAtivo(false)).thenReturn(1L);
        when(profissionalRepository.findAll()).thenReturn(List.of(prof1, prof2, prof3));

        var stats = profissionalService.getDashboardStats();

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalProfissionaisAtivos()).isEqualTo(2L);
        assertThat(stats.getTotalProfissionaisInativos()).isEqualTo(1L);
        assertThat(stats.getPorCategoria()).isNotEmpty();
    }

    @Test
    @DisplayName("Deve contar admitidos nos últimos 30 dias")
    void deveContarAdmitidosUltimos30Dias() {
        Profissional profRecente = Profissional.builder()
                .uuid("uuid-recente")
                .nome("Prof Recente")
                .categoria(CategoriaProfissional.PSICOLOGIA)
                .dataAdmissao(java.time.LocalDate.now().minusDays(15))
                .ativo(true)
                .build();

        Profissional profAntigo = Profissional.builder()
                .uuid("uuid-antigo")
                .nome("Prof Antigo")
                .categoria(CategoriaProfissional.NUTRICAO)
                .dataAdmissao(java.time.LocalDate.now().minusDays(90))
                .ativo(true)
                .build();

        when(profissionalRepository.countByAtivo(true)).thenReturn(2L);
        when(profissionalRepository.countByAtivo(false)).thenReturn(0L);
        when(profissionalRepository.findAll()).thenReturn(List.of(profRecente, profAntigo));

        var stats = profissionalService.getDashboardStats();

        assertThat(stats.getAdmitidosUltimos30Dias()).isEqualTo(1L);
    }

    // ========== New Tests - Additional Coverage ==========

    @Test
    @DisplayName("Deve retornar lista vazia ao buscar por nome inexistente")
    void deveBuscarPorNomeRetornarVazio() {
        when(profissionalRepository.searchByNome("NomeInexistente")).thenReturn(List.of());

        var result = profissionalService.buscarPorNome("NomeInexistente");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve listar apenas profissionais ativos")
    void deveListarApenasAtivos() {
        Profissional ativo1 = Profissional.builder()
                .uuid("uuid-ativo-1")
                .nome("Ativo 1")
                .categoria(CategoriaProfissional.FISIOTERAPIA)
                .ativo(true)
                .build();

        Profissional ativo2 = Profissional.builder()
                .uuid("uuid-ativo-2")
                .nome("Ativo 2")
                .categoria(CategoriaProfissional.ODONTOLOGIA)
                .ativo(true)
                .build();

        when(profissionalRepository.findByAtivoTrue()).thenReturn(List.of(ativo1, ativo2));

        var result = profissionalService.listarAtivos();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getAtivo());
    }

    @Test
    @DisplayName("Deve listar todos incluindo inativos")
    void deveListarTodosIncluindoInativos() {
        Profissional ativo = Profissional.builder()
                .uuid("uuid-ativo")
                .nome("Ativo")
                .categoria(CategoriaProfissional.ASSISTENCIA_SOCIAL)
                .ativo(true)
                .build();

        Profissional inativo = Profissional.builder()
                .uuid("uuid-inativo")
                .nome("Inativo")
                .categoria(CategoriaProfissional.PEDAGOGIA)
                .ativo(false)
                .build();

        when(profissionalRepository.findAll()).thenReturn(List.of(ativo, inativo));

        var result = profissionalService.listarTodos();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Deve buscar por múltiplos campos retornar vazio")
    void deveBuscarPorMultiplosCamposRetornarVazio() {
        when(profissionalRepository.searchByMultipleFields("termo-inexistente")).thenReturn(List.of());

        var result = profissionalService.buscarPorMultiplosCampos("termo-inexistente");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve alternar status de inativo para ativo")
    void deveAlternarStatusDeInativoParaAtivo() {
        profissional.setAtivo(false);
        when(profissionalRepository.findByUuid("uuid-teste")).thenReturn(Optional.of(profissional));

        profissionalService.toggleStatus("uuid-teste", usuarioLogado);

        assertThat(profissional.getAtivo()).isTrue();
        verify(profissionalRepository).save(profissional);
    }
}
