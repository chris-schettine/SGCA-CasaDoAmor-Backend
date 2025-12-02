package br.com.casadoamor.sgca.modules.hospedagem.repository;

import br.com.casadoamor.sgca.modules.hospedagem.entity.Hospedagem;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.AlaQuarto;
import br.com.casadoamor.sgca.modules.hospedagem.entity.enums.StatusHospedagem;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HospedagemSpecification {

    public static Specification<Hospedagem> withFilters(
            String nomePaciente,
            String nomeQuarto,
            AlaQuarto ala,
            StatusHospedagem status,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por nome do paciente (case-insensitive LIKE)
            if (nomePaciente != null && !nomePaciente.trim().isEmpty()) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("paciente").get("dadoPessoal").get("nome")),
                        "%" + nomePaciente.toLowerCase().trim() + "%"
                    )
                );
            }

            // Filtro por nome do quarto (case-insensitive LIKE)
            if (nomeQuarto != null && !nomeQuarto.trim().isEmpty()) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("quarto").get("nome")),
                        "%" + nomeQuarto.toLowerCase().trim() + "%"
                    )
                );
            }

            // Filtro por ala
            if (ala != null) {
                predicates.add(
                    criteriaBuilder.equal(root.get("quarto").get("ala"), ala)
                );
            }

            // Filtro por status
            if (status != null) {
                predicates.add(
                    criteriaBuilder.equal(root.get("status"), status)
                );
            }

            // Filtro por período de entrada
            if (dataInicio != null) {
                predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("dataEntrada"), dataInicio)
                );
            }

            if (dataFim != null) {
                predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(root.get("dataEntrada"), dataFim)
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
