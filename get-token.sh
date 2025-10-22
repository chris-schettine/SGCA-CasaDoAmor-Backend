#!/bin/bash

# Script para obter token JWT do admin
# Uso: ./get-token.sh

echo "=========================================="
echo "  SGCA - Gerador de Token JWT"
echo "=========================================="
echo ""

# Credenciais do admin (pode alterar aqui)
CPF="${1:-00000000000}"
SENHA="${2:-Admin@123}"
API_URL="${3:-http://localhost:8090}"

echo "🔐 Fazendo login..."
echo "   CPF: $CPF"
echo ""

# Faz login e obtém o token
RESPONSE=$(curl -s -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"cpf\": \"$CPF\", \"senha\": \"$SENHA\"}")

# Verifica se houve erro
if echo "$RESPONSE" | jq -e '.message' > /dev/null 2>&1; then
    echo "❌ Erro ao fazer login:"
    echo "$RESPONSE" | jq -r '.message'
    exit 1
fi

# Extrai o token
TOKEN=$(echo "$RESPONSE" | jq -r '.token')
NOME=$(echo "$RESPONSE" | jq -r '.nome')
TIPO=$(echo "$RESPONSE" | jq -r '.tipoUsuario')
EXPIRES=$(echo "$RESPONSE" | jq -r '.expiresIn')

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
    echo "❌ Erro: Token não encontrado na resposta"
    echo "$RESPONSE" | jq .
    exit 1
fi

echo "✅ Login realizado com sucesso!"
echo ""
echo "👤 Usuário: $NOME"
echo "🔑 Tipo: $TIPO"
echo "⏱️  Expira em: $((EXPIRES / 1000 / 60)) minutos"
echo ""
echo "=========================================="
echo "  🎫 SEU TOKEN JWT:"
echo "=========================================="
echo ""
echo "$TOKEN"
echo ""
echo "=========================================="
echo ""
echo "📋 Para usar no Swagger:"
echo "   1. Acesse: $API_URL/swagger-ui/index.html"
echo "   2. Clique no botão 'Authorize' 🔒"
echo "   3. Cole o token acima (sem aspas)"
echo "   4. Clique em 'Authorize'"
echo ""
echo "📋 Para usar no curl:"
echo "   curl -H 'Authorization: Bearer $TOKEN' $API_URL/api/admin/users"
echo ""
echo "💾 Token salvo em: /tmp/sgca_token.txt"
echo "$TOKEN" > /tmp/sgca_token.txt
echo ""
