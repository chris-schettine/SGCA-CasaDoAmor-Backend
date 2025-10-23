package br.com.casadoamor.sgca.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoEnum {
  ACRE("AC"),
  ALAGOAS("AL"),
  AMAPA("AP"),
  AMAZONAS("AM"),
  BAHIA("BA"),
  CEARA("CE"),
  DISTRITO_FEDERAL("DF"),
  ESPIRITO_SANTO("ES"),
  GOIAS("GO"),
  MARANHAO("MA"),
  MATO_GROSSO("MT"),
  MATO_GROSSO_DO_SUL("MS"),
  MINAS_GERAIS("MG"),
  PARA("PA"),
  PARAIBA("PB"),
  PARANA("PR"),
  PERNAMBUCO("PE"),
  PIAUI("PI"),
  RIO_DE_JANEIRO("RJ"),
  RIO_GRANDE_DO_NORTE("RN"),
  RIO_GRANDE_DO_SUL("RS"),
  RONDONIA("RO"),
  RORAIMA("RR"),
  SANTA_CATARINA("SC"),
  SAO_PAULO("SP"),
  SERGIPE("SE"),
  TOCANTINS("TO");
  
  private final String sigla;
  
  EstadoEnum(String sigla) {
    this.sigla = sigla;
  }
  
  @JsonValue
  public String getSigla() {
    return sigla;
  }
  
  @JsonCreator
  public static EstadoEnum fromString(String value) {
    if (value == null) {
      return null;
    }
    
    String upperValue = value.toUpperCase();
    
    // Tenta primeiro pela sigla
    for (EstadoEnum estado : EstadoEnum.values()) {
      if (estado.sigla.equals(upperValue)) {
        return estado;
      }
    }
    
    // Se não encontrar pela sigla, tenta pelo nome do enum
    for (EstadoEnum estado : EstadoEnum.values()) {
      if (estado.name().equals(upperValue)) {
        return estado;
      }
    }
    
    throw new IllegalArgumentException("Estado inválido: " + value);
  }
  
  public static boolean isValid(String estado) {
    if (estado == null) {
      return false;
    }
    
    String upperValue = estado.toUpperCase();
    
    for (EstadoEnum e : EstadoEnum.values()) {
      if (e.name().equals(upperValue) || e.sigla.equals(upperValue)) {
        return true;
      }
    }
    return false;
  }
}
