package br.com.casadoamor.sgca.modules.hospedagem.repository;

import br.com.casadoamor.sgca.modules.hospedagem.entity.Hospedagem;
import br.com.casadoamor.sgca.modules.hospedagem.entity.Quarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.StatusHospedagem;
import br.com.casadoamor.sgca.modules.common.entity.DadoPessoal;
import br.com.casadoamor.sgca.modules.paciente.entity.Paciente;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospedagemSpecificationTest {

    @Mock
    private Root<Hospedagem> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> pacientePath;

    @Mock
    private Path<Object> dadoPessoalPath;

    @Mock
    private Path<String> nomePath;

    @Mock
    private Path<Object> quartoPath;

    @Mock
    private Path<String> nomeQuartoPath;

    @Mock
    private Path<AlaQuarto> alaPath;

    @Mock
    private Path<StatusHospedagem> statusPath;

    @Mock
    private Path<LocalDate> dataEntradaPath;

    @Mock
    private Expression<String> lowerExpression;

    @Mock
    private Predicate predicate;

    @Mock
    private Predicate conjunctionPredicate;

    @BeforeEach
    void setUp() {
        lenient().when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Nested
    @DisplayName("Testes sem filtros")
    class SemFiltrosTests {

        @Test
        @DisplayName("Deve retornar specification sem predicados quando todos os parâmetros são nulos")
        void deveRetornarSpecificationSemPredicados() {
            Specification<Hospedagem> spec = HospedagemSpecification.withFilters(
                    null, null, null, null, null, null
            );

            assertNotNull(spec);
            
            Predicate result = spec.toPredicate(root, query, criteriaBuilder);
            
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Testes de filtro por nome do paciente")
    class FiltroNomePacienteTests {

        @Test
        @DisplayName("Deve adicionar predicado para nome do paciente")
        @SuppressWarnings("unchecked")
        void deveAdicionarPredicadoParaNomePaciente() {
            when(root.get("paciente")).thenReturn(pacientePath);
            when(pacientePath.get("dadoPessoal")).thenReturn(dadoPessoalPath);
            when(dadoPessoalPath.get("nome")).thenReturn((Path) nomePath);
            when(criteriaBuilder.lower(any())).thenReturn(lowerExpression);
            when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(predicate);

            Specification<Hospedagem> spec = HospedagemSpecification.withFilters(
                    "João", null, null, null, null, null
            );

            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(result);
            verify(criteriaBuilder).lower(any());
            verify(criteriaBuilder).like(any(Expression.class), eq("%joão%"));
        }

        @Test
        @DisplayName("Deve ignorar nome do paciente em branco")
        void deveIgnorarNomePacienteEmBranco() {
            Specification<Hospedagem> spec = HospedagemSpecification.withFilters(
                    "   ", null, null, null, null, null
            );

            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(result);
            verify(root, never()).get("paciente");
        }
    }

    @Nested
    @DisplayName("Testes de filtro por nome do quarto")
    class FiltroNomeQuartoTests {

        @Test
        @DisplayName("Deve adicionar predicado para nome do quarto")
        @SuppressWarnings("unchecked")
        void deveAdicionarPredicadoParaNomeQuarto() {
            when(root.get("quarto")).thenReturn(quartoPath);
            when(quartoPath.get("nome")).thenReturn((Path) nomeQuartoPath);
            when(criteriaBuilder.lower(any())).thenReturn(lowerExpression);
            when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(predicate);

            Specification<Hospedagem> spec = HospedagemSpecification.withFilters(
                    null, "Quarto 101", null, null, null, null
            );

            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(result);
            verify(criteriaBuilder).like(any(Expression.class), eq("%quarto 101%"));
        }

        @Test
        @DisplayName("Deve ignorar nome do quarto em branco")
        void deveIgnorarNomeQuartoEmBranco() {
            Specification<Hospedagem> spec = HospedagemSpecification.withFilters(
                    null, "", null, null, null, null
            );

            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(result);
            verify(root, never()).get("quarto");
        }
    }

    @Nested
    @DisplayName("Testes de filtro por ala")
    class FiltroAlaTests {

        @Test
        @DisplayName("Deve adicionar predicado para ala")
        @SuppressWarnings("unchecked")
        void deveAdicionarPredicadoParaAla() {
            when(root.get("quarto")).thenReturn(quartoPath);
            when(quartoPath.get("ala")).thenReturn((Path) alaPath);
            when(criteriaBuilder.equal(any(), any(AlaQuarto.class))).thenReturn(predicate);

            Specification<Hospedagem> spec = HospedagemSpecification.withFilters(
                    null, null, AlaQuarto.MASCULINA, null, null, null
            );

            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(result);
            verify(criteriaBuilder).equal(any(), eq(AlaQuarto.MASCULINA));
        }
    }

    @Nested
    @DisplayName("Testes de filtro por status")
    class FiltroStatusTests {

        @Test
        @DisplayName("Deve adicionar predicado para status")
        @SuppressWarnings("unchecked")
        void deveAdicionarPredicadoParaStatus() {
            when(root.get("status")).thenReturn((Path) statusPath);
            when(criteriaBuilder.equal(any(), any(StatusHospedagem.class))).thenReturn(predicate);

            Specification<Hospedagem> spec = HospedagemSpecification.withFilters(
                    null, null, null, StatusHospedagem.ATIVA, null, null
            );

            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(result);
            verify(criteriaBuilder).equal(any(), eq(StatusHospedagem.ATIVA));
        }
    }

    @Nested
    @DisplayName("Testes de filtro por período")
    class FiltroPeriodoTests {

        @Test
        @DisplayName("Deve adicionar predicado para data início")
        @SuppressWarnings("unchecked")
        void deveAdicionarPredicadoParaDataInicio() {
            LocalDate dataInicio = LocalDate.of(2024, 1, 1);
            when(root.get("dataEntrada")).thenReturn((Path) dataEntradaPath);
            when(criteriaBuilder.greaterThanOrEqualTo(any(), any(LocalDate.class))).thenReturn(predicate);

            Specification<Hospedagem> spec = HospedagemSpecification.withFilters(
                    null, null, null, null, dataInicio, null
            );

            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(result);
            verify(criteriaBuilder).greaterThanOrEqualTo(any(), eq(dataInicio));
        }

        @Test
        @DisplayName("Deve adicionar predicado para data fim")
        @SuppressWarnings("unchecked")
        void deveAdicionarPredicadoParaDataFim() {
            LocalDate dataFim = LocalDate.of(2024, 12, 31);
            when(root.get("dataEntrada")).thenReturn((Path) dataEntradaPath);
            when(criteriaBuilder.lessThanOrEqualTo(any(), any(LocalDate.class))).thenReturn(predicate);

            Specification<Hospedagem> spec = HospedagemSpecification.withFilters(
                    null, null, null, null, null, dataFim
            );

            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(result);
            verify(criteriaBuilder).lessThanOrEqualTo(any(), eq(dataFim));
        }

        @Test
        @DisplayName("Deve adicionar predicados para ambas as datas")
        @SuppressWarnings("unchecked")
        void deveAdicionarPredicadosParaAmbasDatas() {
            LocalDate dataInicio = LocalDate.of(2024, 1, 1);
            LocalDate dataFim = LocalDate.of(2024, 12, 31);
            when(root.get("dataEntrada")).thenReturn((Path) dataEntradaPath);
            when(criteriaBuilder.greaterThanOrEqualTo(any(), any(LocalDate.class))).thenReturn(predicate);
            when(criteriaBuilder.lessThanOrEqualTo(any(), any(LocalDate.class))).thenReturn(predicate);

            Specification<Hospedagem> spec = HospedagemSpecification.withFilters(
                    null, null, null, null, dataInicio, dataFim
            );

            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(result);
            verify(criteriaBuilder).greaterThanOrEqualTo(any(), eq(dataInicio));
            verify(criteriaBuilder).lessThanOrEqualTo(any(), eq(dataFim));
        }
    }

    @Nested
    @DisplayName("Testes com múltiplos filtros")
    class MultiplosFiltrosTests {

        @Test
        @DisplayName("Deve combinar todos os filtros")
        @SuppressWarnings("unchecked")
        void deveCombinarTodosFiltros() {
            LocalDate dataInicio = LocalDate.of(2024, 1, 1);
            LocalDate dataFim = LocalDate.of(2024, 12, 31);

            when(root.get("paciente")).thenReturn(pacientePath);
            when(pacientePath.get("dadoPessoal")).thenReturn(dadoPessoalPath);
            when(dadoPessoalPath.get("nome")).thenReturn((Path) nomePath);
            when(root.get("quarto")).thenReturn(quartoPath);
            when(quartoPath.get("nome")).thenReturn((Path) nomeQuartoPath);
            when(quartoPath.get("ala")).thenReturn((Path) alaPath);
            when(root.get("status")).thenReturn((Path) statusPath);
            when(root.get("dataEntrada")).thenReturn((Path) dataEntradaPath);
            lenient().when(criteriaBuilder.lower(any())).thenReturn(lowerExpression);
            lenient().when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(predicate);
            lenient().when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
            lenient().when(criteriaBuilder.greaterThanOrEqualTo(any(), any(LocalDate.class))).thenReturn(predicate);
            lenient().when(criteriaBuilder.lessThanOrEqualTo(any(), any(LocalDate.class))).thenReturn(predicate);

            Specification<Hospedagem> spec = HospedagemSpecification.withFilters(
                    "João", "Quarto 101", AlaQuarto.MASCULINA, StatusHospedagem.ATIVA, dataInicio, dataFim
            );

            Predicate result = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(result);
            // Verificar que lower foi chamado para nome do paciente e do quarto
            verify(criteriaBuilder, times(2)).lower(any());
            // Verificar que like foi chamado para nome do paciente e do quarto
            verify(criteriaBuilder, times(2)).like(any(Expression.class), anyString());
            // Verificar que equal foi chamado para ala e status
            verify(criteriaBuilder).equal(eq(alaPath), eq(AlaQuarto.MASCULINA));
            verify(criteriaBuilder).equal(eq(statusPath), eq(StatusHospedagem.ATIVA));
            // Verificar filtros de data
            verify(criteriaBuilder).greaterThanOrEqualTo(any(), eq(dataInicio));
            verify(criteriaBuilder).lessThanOrEqualTo(any(), eq(dataFim));
        }
    }
}
