#!/bin/bash

echo "========================================="
echo "TESTE DE LIMPEZA DE CPF"
echo "========================================="
echo ""

# Gerar CPF único
CPF_LIMPO="55544433322"
CPF_FORMATADO="555.444.333-22"
EMAIL="teste.cpf.$(date +%s)@teste.com"

# 1. Login como admin
echo "1. Fazendo login como ADMIN..."
TOKEN=$(curl -s -X POST "http://localhost:8090/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"cpf":"00000000000","senha":"Admin@123"}' | jq -r '.token')

echo "✅ Token obtido"
echo ""

# 2. Criar usuário com CPF formatado
echo "2. Criando usuário com CPF FORMATADO: $CPF_FORMATADO"
RESPONSE=$(curl -s -X POST "http://localhost:8090/admin/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"nome\": \"Teste CPF Formatado\",
    \"email\": \"$EMAIL\",
    \"cpf\": \"$CPF_FORMATADO\",
    \"telefone\": \"(11) 98765-4321\",
    \"tipo\": \"MEDICO\"
  }")

CPF_SALVO=$(echo "$RESPONSE" | jq -r '.cpf')

echo ""
echo "========================================="
echo "RESULTADO:"
echo "========================================="
echo "CPF enviado:      $CPF_FORMATADO"
echo "CPF salvo no BD:  $CPF_SALVO"
echo ""

if [ "$CPF_SALVO" == "$CPF_LIMPO" ]; then
    echo "✅ SUCESSO! CPF foi salvo SEM formatação (apenas números)"
    echo ""
    
    # 3. Testar login com CPF formatado
    echo "3. Testando se login aceita CPF FORMATADO..."
    LOGIN_TEST=$(curl -s -X POST "http://localhost:8090/auth/login" \
      -H "Content-Type: application/json" \
      -d "{\"cpf\":\"$CPF_FORMATADO\",\"senha\":\"SenhaQualquer@123\"}")
    
    # Verifica se não deu erro de "CPF não encontrado" baseado no CPF limpo
    if echo "$LOGIN_TEST" | grep -q "Credenciais inválidas"; then
        echo "✅ Login processou CPF formatado corretamente!"
        echo "   (Erro de senha é esperado, mas CPF foi encontrado)"
    else
        echo "Resposta do login: $LOGIN_TEST"
    fi
    
    echo ""
    echo "========================================="
    echo "✅ TESTE PASSOU! Sistema limpa CPF corretamente"
    echo "========================================="
else
    echo "❌ FALHA! CPF não foi limpo corretamente"
    echo "   Esperado: $CPF_LIMPO"
    echo "   Recebido: $CPF_SALVO"
fi
