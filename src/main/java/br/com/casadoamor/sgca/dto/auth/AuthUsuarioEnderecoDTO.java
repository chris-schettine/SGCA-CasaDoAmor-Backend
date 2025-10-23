package br.com.casadoamor.sgca.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para endereço de usuário do sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUsuarioEnderecoDTO {

    private Long id;

    @NotBlank(message = "Logradouro é obrigatório")
    @Size(max = 150, message = "Logradouro deve ter no máximo 150 caracteres")
    private String logradouro;

    @Size(max = 10, message = "Número deve ter no máximo 10 caracteres")
    private String numero;

    @Size(max = 150, message = "Complemento deve ter no máximo 150 caracteres")
    private String complemento;

    @NotBlank(message = "Bairro é obrigatório")
    @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória")
    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    private String cidade;

    @NotBlank(message = "UF é obrigatória")
    @Pattern(regexp = "^[A-Z]{2}$", message = "UF deve ter 2 letras maiúsculas")
    private String uf;

    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP deve estar no formato 12345-678 ou 12345678")
    private String cep;
}
