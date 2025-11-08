ALTER TABLE pacientes
  CHANGE COLUMN caminhoDaImagemNoBucket caminho_da_imagem_no_bucket VARCHAR(512);

ALTER TABLE dados_clinicos
ADD COLUMN tipo_sanguineo ENUM(
  'A_POSITIVO', 'A_NEGATIVO',
  'B_POSITIVO', 'B_NEGATIVO',
  'AB_POSITIVO', 'AB_NEGATIVO',
  'O_POSITIVO', 'O_NEGATIVO'
) NOT NULL;

ALTER TABLE dados_pessoais
ADD COLUMN estado_civil ENUM(
  'SOLTEIRO',
  'CASADO',
  'DIVORCIADO',
  'VIUVO',
  'SEPARADO',
  'UNIAO_ESTAVEL'
) DEFAULT NULL;
