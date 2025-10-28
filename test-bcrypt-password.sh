#!/bin/bash

# Script para testar senha BCrypt

HASH='$2a$12$qbIC3NA71l2f5cGMhRrIKO.53MWP5o7I12gdYfqI.Mt/CG6MlvBCG'

echo "Testando senha Admin@123..."
curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "00000000000",
    "senha": "Admin@123"
  }' | jq .

echo -e "\n\nTestando senha Admin@1234..."
curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "00000000000",
    "senha": "Admin@1234"
  }' | jq .
