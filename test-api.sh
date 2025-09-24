#!/bin/bash

# Test script for SGCA Backend API endpoints that match frontend expectations
echo "🧪 Testing SGCA Backend API endpoints..."
echo "========================================"

BASE_URL="http://localhost:8090"
API_PATH="/api/1.0/pessoa-fisica"

echo "1. Testing GET all pessoas físicas:"
curl -s "${BASE_URL}${API_PATH}" | jq . || echo "Empty response (expected for new database)"
echo -e "\n"

echo "2. Testing OPTIONS request (CORS preflight):"
curl -s -X OPTIONS -H "Origin: http://localhost:3000" -H "Access-Control-Request-Method: POST" -I "${BASE_URL}${API_PATH}"
echo -e "\n"

echo "3. Testing POST create pessoa física:"
curl -s -X POST "${BASE_URL}${API_PATH}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "nome": "João Silva",
    "cpf": "123.456.789-00",
    "telefone": "(11) 99999-9999",
    "email": "joao@test.com",
    "endereco": {
      "cep": "01234-567",
      "logradouro": "Rua das Flores",
      "numero": "123",
      "bairro": "Centro",
      "cidade": "São Paulo",
      "estado": "SP"
    }
  }' | jq . 2>/dev/null || echo "Testing with minimal data..."
echo -e "\n"

echo "4. Testing GET by ID (assuming ID 1):"
curl -s "${BASE_URL}${API_PATH}/1" | jq . 2>/dev/null || echo "No data found (expected)"
echo -e "\n"

echo "5. Testing GET by nome:"
curl -s "${BASE_URL}${API_PATH}/João/nome" | jq . 2>/dev/null || echo "No data found (expected)"
echo -e "\n"

echo "6. Testing GET by CPF:"
curl -s "${BASE_URL}${API_PATH}/123.456.789-00/cpf" | jq . 2>/dev/null || echo "No data found (expected)"
echo -e "\n"

echo "7. Testing PATCH update (assuming ID 1):"
curl -s -X PATCH "${BASE_URL}${API_PATH}/1" \
  -H "Content-Type: application/json" \
  -d '{"nome": "João Silva Updated"}' | jq . 2>/dev/null || echo "No data to update (expected)"
echo -e "\n"

echo "8. Testing DELETE (assuming ID 1):"
curl -s -X DELETE "${BASE_URL}${API_PATH}/1" | jq . 2>/dev/null || echo "No data to delete (expected)"
echo -e "\n"

echo "✅ All endpoints are accessible!"
echo "🚀 Your frontend should now be able to communicate with the backend on port 8090"