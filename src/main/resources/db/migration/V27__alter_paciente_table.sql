ALTER TABLE pacientes
  ADD COLUMN email VARCHAR(255) UNIQUE;

ALTER TABLE pacientes
  ADD COLUMN caminhoDaImagemNoBucket VARCHAR(512);
