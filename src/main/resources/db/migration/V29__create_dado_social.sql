CREATE TABLE dados_sociais (
  id CHAR(36) NOT NULL PRIMARY KEY,
  renda_familiar DECIMAL(10,2),
  composicao_familiar VARCHAR(255),
  situacao_moradia VARCHAR(100),
  necessidades_especiais VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
