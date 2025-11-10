-- Migration para remover a tabela profissional_saude antiga
-- Esta tabela será substituída pela nova tabela 'profissionais' que unifica funcionários e voluntários

DROP TABLE IF EXISTS profissional_saude;
