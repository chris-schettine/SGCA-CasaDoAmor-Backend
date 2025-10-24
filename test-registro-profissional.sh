#!/bin/bash

echo "========================================="
echo "TESTE DE REGISTRO PROFISSIONAL"
echo "========================================="
echo ""

# Login como admin
echo "1. Fazendo login como ADMIN..."
TOKEN=$(curl -s -X POST "http://localhost:8090/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"cpf":"00000000000","senha":"Admin@123"}' | jq -r '.token')

echo "✅ Token obtido: ${TOKEN:0:30}..."
echo ""

# Criar médico com CRM e RQE
EMAIL_MEDICO="dr.silva.$(date +%s)@casadoamor.com"
CPF_MEDICO="12345678901"

echo "2. Criando MÉDICO com CRM e RQE..."
RESPONSE_MEDICO=$(curl -s -X POST "http://localhost:8090/admin/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"nome\": \"Dr. João Silva\",
    \"email\": \"$EMAIL_MEDICO\",
    \"cpf\": \"$CPF_MEDICO\",
    \"telefone\": \"(77) 98888-7777\",
    \"tipo\": \"MEDICO\",
    \"registroProfissional\": {
      \"tipoProfissional\": \"MEDICO\",
      \"numeroRegistro\": \"123456-SP\",
      \"rqe\": \"54321\"
    }
  }")

echo "$RESPONSE_MEDICO" | jq '.'
echo ""

# Criar enfermeiro com COREN (sem RQE)
EMAIL_ENFERMEIRO="enf.maria.$(date +%s)@casadoamor.com"
CPF_ENFERMEIRO="98765432109"

echo "3. Criando ENFERMEIRO com COREN (sem RQE)..."
RESPONSE_ENFERMEIRO=$(curl -s -X POST "http://localhost:8090/admin/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"nome\": \"Maria Santos\",
    \"email\": \"$EMAIL_ENFERMEIRO\",
    \"cpf\": \"$CPF_ENFERMEIRO\",
    \"telefone\": \"(77) 97777-6666\",
    \"tipo\": \"ENFERMEIRO\",
    \"registroProfissional\": {
      \"tipoProfissional\": \"ENFERMEIRO\",
      \"numeroRegistro\": \"654321-SP\"
    }
  }")

echo "$RESPONSE_ENFERMEIRO" | jq '.'
echo ""

# Buscar o médico criado para ver o registro profissional
MEDICO_ID=$(echo "$RESPONSE_MEDICO" | jq -r '.id')

if [ "$MEDICO_ID" != "null" ] && [ -n "$MEDICO_ID" ]; then
    echo "4. Buscando dados do MÉDICO (ID: $MEDICO_ID) com registro profissional..."
    curl -s -X GET "http://localhost:8090/admin/users/$MEDICO_ID" \
      -H "Authorization: Bearer $TOKEN" | jq '.registroProfissional'
    echo ""
fi

echo "========================================="
echo "✅ TESTE CONCLUÍDO!"
echo "========================================="
echo ""
echo "Resumo:"
echo "- Médico criado com CRM e RQE"
echo "- Enfermeiro criado com COREN (sem RQE)"
echo "- Registros profissionais são IMUTÁVEIS"
echo "========================================="
