package br.com.casadoamor.sgca.util;

/**
 * Utilitários para manipulação de CPF
 */
public class CpfUtil {

    /**
     * Remove pontos, hífens e espaços do CPF, deixando apenas números
     * 
     * @param cpf CPF formatado (ex: 123.456.789-00)
     * @return CPF sem formatação (ex: 12345678900)
     */
    public static String limparCpf(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("[^0-9]", "");
    }

    /**
     * Remove caracteres especiais de telefone, deixando apenas números
     * 
     * @param telefone Telefone formatado (ex: (11) 98765-4321)
     * @return Telefone sem formatação (ex: 11987654321)
     */
    public static String limparTelefone(String telefone) {
        if (telefone == null) {
            return null;
        }
        return telefone.replaceAll("[^0-9]", "");
    }
}
