package br.com.casadoamor.sgca.infra.util;

public class RgUtil {

  public static String limparRg(String rg) {
      if (rg == null) {
          return null;
      }
      return rg.replaceAll("[^0-9]", "");
  }
}
