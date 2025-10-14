package br.com.casadoamor.sgca.enums;

public enum EstadoEnum {
  ACRE,
  ALAGOAS,
  AMAPA,
  AMAZONAS,
  BAHIA,
  CEARA,
  DISTRITO_FEDERAL,
  ESPIRITO_SANTO,
  GOIAS,
  MARANHAO,
  MATO_GROSSO,
  MATO_GROSSO_DO_SUL,
  MINAS_GERAIS,
  PARA,
  PARAIBA,
  PARANA,
  PERNAMBUCO,
  PIAUI,
  RIO_DE_JANEIRO,
  RIO_GRANDE_DO_NORTE,
  RIO_GRANDE_DO_SUL,
  RONDONIA,
  RORAIMA,
  SANTA_CATARINA,
  SAO_PAULO,
  SERGIPE,
  TOCANTINS;
  
  public static boolean isValid(String estado) {
    for (EstadoEnum e : EstadoEnum.values()) {
      if (e.name().equalsIgnoreCase(estado)) {
        return true;
      }
    }
    return false;
  }
}
