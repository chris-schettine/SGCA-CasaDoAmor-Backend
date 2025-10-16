package br.com.casadoamor.sgca.dto.twofactor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para habilitar/desabilitar 2FA
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enable2FADTO {

    @NotBlank(message = "Código 2FA é obrigatório")
    @Pattern(regexp = "^[0-9]{6}$", message = "Código deve conter exatamente 6 dígitos")
    private String codigo;

    private Boolean habilitar = true; // true = habilitar, false = desabilitar
}
