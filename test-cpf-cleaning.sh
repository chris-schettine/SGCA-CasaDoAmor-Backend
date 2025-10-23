#!/bin/bash

# Script para testar a limpeza de CPF

BASE_URL="http://localhost:8090"

echo "==================================="
echo "Teste de Limpeza de CPF"
echo "==================================="
echo ""

# 1. Registrar um novo usuário com CPF formatado
echo "1. Testando registro com CPF formatado (039.844.175-86)..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Teste CPF Formatado",
    "email": "teste.cpf.formatado@teste.com",
    "cpf": "039.844.175-86",
    "telefone": "(11) 98765-4321",
    "senha": "Teste@123",
    "confirmarSenha": "Teste@123"
  }')

if echo "$REGISTER_RESPONSE" | grep -q "token"; then
    echo "✅ Registro bem-sucedido!"
    echo "Resposta: $REGISTER_RESPONSE" | jq '.'
else
    echo "❌ Falha no registro"
    echo "Resposta: $REGISTER_RESPONSE"
fi

echo ""
echo "-----------------------------------"
echo ""

# 2. Tentar fazer login com CPF formatado
echo "2. Testando login com CPF formatado (039.844.175-86)..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "039.844.175-86",
    "senha": "Teste@123"
  }')

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    echo "✅ Login com CPF formatado bem-sucedido!"
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
    echo "Token obtido: ${TOKEN:0:20}..."
else
    echo "❌ Falha no login com CPF formatado"
    echo "Resposta: $LOGIN_RESPONSE"
fi

echo ""
echo "-----------------------------------"
echo ""

# 3. Tentar fazer login com CPF sem formatação
echo "3. Testando login com CPF limpo (03984417586)..."
LOGIN_RESPONSE_2=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "03984417586",
    "senha": "Teste@123"
  }')

if echo "$LOGIN_RESPONSE_2" | grep -q "token"; then
    echo "✅ Login com CPF limpo bem-sucedido!"
    echo "Token obtido: $(echo "$LOGIN_RESPONSE_2" | jq -r '.token' | cut -c1-20)..."
else
    echo "❌ Falha no login com CPF limpo"
    echo "Resposta: $LOGIN_RESPONSE_2"
fi

echo ""
echo "==================================="
echo "Teste concluído!"
echo "==================================="
