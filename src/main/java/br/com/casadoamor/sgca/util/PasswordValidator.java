package br.com.casadoamor.sgca.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utilitário para validação de senhas com política rigorosa
 * 
 * Política de Senhas:
 * - Mínimo 8 caracteres
 * - Pelo menos 1 letra maiúscula
 * - Pelo menos 1 letra minúscula
 * - Pelo menos 1 número
 * - Pelo menos 1 caractere especial
 */
public class PasswordValidator {

    private static final int TAMANHO_MINIMO = 8;
    private static final Pattern MAIUSCULA_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern MINUSCULA_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern NUMERO_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern ESPECIAL_PATTERN = Pattern.compile("[^A-Za-z0-9]"); // Qualquer caractere que não seja letra ou número
    
    /**
     * Valida se a senha atende aos critérios de segurança
     * 
     * @param senha Senha a ser validada
     * @return ValidationResult com status e mensagens de erro
     */
    public static ValidationResult validar(String senha) {
        List<String> erros = new ArrayList<>();
        
        // Verifica se senha é nula ou vazia
        if (senha == null || senha.trim().isEmpty()) {
            erros.add("Senha não pode ser vazia");
            return new ValidationResult(false, erros);
        }
        
        // Remove espaços em branco para validação
        String senhaSemEspacos = senha.trim();
        
        // Validação 1: Tamanho mínimo
        if (senhaSemEspacos.length() < TAMANHO_MINIMO) {
            erros.add(String.format("Senha deve ter no mínimo %d caracteres", TAMANHO_MINIMO));
        }
        
        // Validação 2: Letra maiúscula
        if (!MAIUSCULA_PATTERN.matcher(senhaSemEspacos).find()) {
            erros.add("Senha deve conter pelo menos uma letra MAIÚSCULA");
        }
        
        // Validação 3: Letra minúscula
        if (!MINUSCULA_PATTERN.matcher(senhaSemEspacos).find()) {
            erros.add("Senha deve conter pelo menos uma letra minúscula");
        }
        
        // Validação 4: Número
        if (!NUMERO_PATTERN.matcher(senhaSemEspacos).find()) {
            erros.add("Senha deve conter pelo menos um número");
        }
        
        // Validação 5: Caractere especial
        if (!ESPECIAL_PATTERN.matcher(senhaSemEspacos).find()) {
            erros.add("Senha deve conter pelo menos um caractere especial (@, #, $, %, &, *, etc.)");
        }
        
        boolean valida = erros.isEmpty();
        return new ValidationResult(valida, erros);
    }
    
    /**
     * Valida e lança exceção se inválida
     * 
     * @param senha Senha a ser validada
     * @throws IllegalArgumentException se senha for inválida
     */
    public static void validarOuLancarExcecao(String senha) {
        ValidationResult resultado = validar(senha);
        if (!resultado.isValida()) {
            throw new IllegalArgumentException(resultado.getMensagemErro());
        }
    }
    
    /**
     * Classe para resultado da validação
     */
    public static class ValidationResult {
        private final boolean valida;
        private final List<String> erros;
        
        public ValidationResult(boolean valida, List<String> erros) {
            this.valida = valida;
            this.erros = erros;
        }
        
        public boolean isValida() {
            return valida;
        }
        
        public List<String> getErros() {
            return erros;
        }
        
        public String getMensagemErro() {
            if (erros.isEmpty()) {
                return "";
            }
            return String.join("; ", erros);
        }
        
        public String getMensagemErroFormatada() {
            if (erros.isEmpty()) {
                return "";
            }
            return "Política de senha não atendida:\n- " + String.join("\n- ", erros);
        }
    }
    
    /**
     * Retorna a política de senhas como string (para documentação)
     */
    public static String getPoliticaSenha() {
        return String.format(
            "Política de Senhas:\n" +
            "- Mínimo de %d caracteres\n" +
            "- Pelo menos 1 letra MAIÚSCULA\n" +
            "- Pelo menos 1 letra minúscula\n" +
            "- Pelo menos 1 número\n" +
            "- Pelo menos 1 caractere especial (@, #, $, %%, &, *, etc.)",
            TAMANHO_MINIMO
        );
    }
}
