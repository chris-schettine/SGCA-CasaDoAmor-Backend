package br.com.casadoamor.sgca.infra.util;

public class TelefoneUtil {

  public static String limparTelefone(String telefone) {
    if (telefone == null) {
      return null;
    }
    return telefone.replaceAll("[^0-9]", "");
  }
}
