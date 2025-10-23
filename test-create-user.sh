#!/bin/bash

# Obter token do admin
TOKEN=$(curl -s -X POST "http://localhost:8090/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"cpf":"00000000000","senha":"Admin@123"}' | jq -r '.token')

echo "Token obtido: ${TOKEN:0:30}..."
echo ""

# Criar usuário com CPF formatado
echo "Criando usuário com CPF formatado (111.222.333-44)..."
curl -s -X POST "http://localhost:8090/admin/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "nome": "Teste CPF Formatado",
    "email": "teste.cpf.limpo2@teste.com",
    "cpf": "111.222.333-44",
    "telefone": "(11) 98765-4321",
    "tipo": "PACIENTE"
  }' | jq '.'

echo ""
echo "Usuário criado! Agora tentando fazer login com CPF formatado..."
echo ""

# Aguardar criação de senha temporária (se houver)
sleep 2

# Tentar login com CPF formatado
curl -s -X POST "http://localhost:8090/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"cpf":"111.222.333-44","senha":"senha_temporaria_aqui"}' | jq '.'
