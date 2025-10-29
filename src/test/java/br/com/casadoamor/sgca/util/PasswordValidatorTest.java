package br.com.casadoamor.sgca.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordValidatorTest {

    @Test
    void PasswordValidator_validate_ValidStrongPassword_ReturnsTrue() {
        PasswordValidator.ValidationResult result = PasswordValidator.validar("Aa1@abcd");
        assertTrue(result.isValida());
        assertTrue(result.getErros().isEmpty());
    }

    @Test
    void PasswordValidator_validate_MissingUppercase_ReturnsFalse() {
        PasswordValidator.ValidationResult result = PasswordValidator.validar("aa1@abcd");
        assertFalse(result.isValida());
        assertTrue(result.getErros().stream().anyMatch(s -> s.toLowerCase().contains("mai")));
    }

    @Test
    void PasswordValidator_validate_TooShort_ReturnsFalse() {
        PasswordValidator.ValidationResult result = PasswordValidator.validar("Aa1@a");
        assertFalse(result.isValida());
           // message contains "mínimo" (accented) - match either accented or plain substring
           assertTrue(result.getErros().stream().anyMatch(s -> s.toLowerCase().contains("mínimo") || s.toLowerCase().contains("min")));
    }

    @Test
    void PasswordValidator_validate_NullOrEmpty_ReturnsFalse() {
        PasswordValidator.ValidationResult result = PasswordValidator.validar(null);
        assertFalse(result.isValida());
        assertTrue(result.getErros().stream().anyMatch(s -> s.toLowerCase().contains("vazia") || s.toLowerCase().contains("vaz")));
    }

    @Test
    void PasswordValidator_validarOuLancarExcecao_Invalid_ThrowsIllegalArgumentException() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> PasswordValidator.validarOuLancarExcecao("short"));
    assertTrue(ex.getMessage() != null && !ex.getMessage().isBlank());
    }
}
