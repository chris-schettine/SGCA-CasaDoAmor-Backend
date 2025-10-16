package br.com.casadoamor.sgca.dto.twofactor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para verificar código 2FA durante o login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Verify2FADTO {

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "^[0-9]{11}$", message = "CPF deve conter 11 dígitos")
    private String cpf;

    @NotBlank(message = "Código 2FA é obrigatório")
    @Pattern(regexp = "^[0-9]{6}$", message = "Código deve conter exatamente 6 dígitos")
    private String codigo;
}
