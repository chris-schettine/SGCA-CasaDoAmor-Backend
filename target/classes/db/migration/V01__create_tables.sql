CREATE TABLE dados_pessoais (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  nome VARCHAR(255) NOT NULL,
  data_nascimento DATE,
  cpf VARCHAR(11) UNIQUE NOT NULL,
  rg VARCHAR(10) UNIQUE,
  naturalidade VARCHAR(255),
  profissao VARCHAR(255),
  telefone VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE TABLE enderecos (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  logradouro VARCHAR(150) NOT NULL,
  bairro VARCHAR(100) NOT NULL,
  numero INT,
  cidade VARCHAR(100) NOT NULL,
  estado VARCHAR(50) NOT NULL,
  cep VARCHAR(10),
  complemento VARCHAR(150),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE TABLE pacientes (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  dado_pessoal_id CHAR(36),
  endereco_id CHAR(36),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  CONSTRAINT fk_paciente_dado_pessoal FOREIGN KEY (dado_pessoal_id) REFERENCES dados_pessoais(id) ON DELETE SET NULL,
  CONSTRAINT fk_paciente_endereco FOREIGN KEY (endereco_id) REFERENCES enderecos(id) ON DELETE SET NULL
);

CREATE TABLE dados_clinicos (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  diagnostico VARCHAR(255),
  tratamento VARCHAR(255),
  usa_sonda BOOLEAN NOT NULL,
  tipo_sonda VARCHAR(255),
  usa_curativo BOOLEAN NOT NULL,
  paciente_id CHAR(36) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  CONSTRAINT fk_dado_clinico_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE
);

CREATE TABLE acompanhantes (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  pode_ajudar_na_cozinha BOOLEAN NOT NULL,
  dado_pessoal_id CHAR(36),
  endereco_id CHAR(36),
  ativo BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  CONSTRAINT fk_acompanhante_dado_pessoal FOREIGN KEY (dado_pessoal_id) REFERENCES dados_pessoais(id) ON DELETE SET NULL,
  CONSTRAINT fk_acompanhante_endereco FOREIGN KEY (endereco_id) REFERENCES enderecos(id) ON DELETE SET NULL
);

CREATE TABLE profissional_saude (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  email VARCHAR(255) UNIQUE NOT NULL,
  senha VARCHAR(255) NOT NULL,
  ativo BOOLEAN DEFAULT TRUE,
  tipo VARCHAR(50) NOT NULL,
  documento VARCHAR(255) NOT NULL,
  uf_documento CHAR(2) NOT NULL,
  especialidade VARCHAR(255),
  dado_pessoal_id CHAR(36) NOT NULL,
  endereco_id CHAR(36),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  CONSTRAINT fk_profissional_dado_pessoal FOREIGN KEY (dado_pessoal_id) REFERENCES dados_pessoais(id) ON DELETE CASCADE,
  CONSTRAINT fk_profissional_endereco FOREIGN KEY (endereco_id) REFERENCES enderecos(id) ON DELETE SET NULL
);

CREATE TABLE usuarios (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  email VARCHAR(255) UNIQUE NOT NULL,
  senha VARCHAR(255) NOT NULL,
  ativo BOOLEAN DEFAULT TRUE,
  tipo_usuario VARCHAR(50) NOT NULL,
  dado_pessoal_id CHAR(36),
  endereco_id CHAR(36),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  CONSTRAINT fk_usuario_dado_pessoal FOREIGN KEY (dado_pessoal_id) REFERENCES dados_pessoais(id) ON DELETE SET NULL,
  CONSTRAINT fk_usuario_endereco FOREIGN KEY (endereco_id) REFERENCES enderecos(id) ON DELETE SET NULL
);

CREATE TABLE recebeu_alta (
  id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
  recebeu_alta BOOLEAN DEFAULT TRUE,
  data TIMESTAMP,
  paciente_id CHAR(36),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP,
  CONSTRAINT fk_recebeu_alta_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE
);
