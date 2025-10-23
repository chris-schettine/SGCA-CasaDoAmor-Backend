#!/bin/bash

# Script para testar endpoints de perfil de usuário com dados pessoais e endereço

TOKEN_FILE="/tmp/sgca_token.txt"
BASE_URL="http://localhost:8090"
API_ADMIN="${BASE_URL}/admin"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Verifica se o token existe
if [ ! -f "$TOKEN_FILE" ]; then
    echo -e "${RED}❌ Token não encontrado. Execute ./get-token.sh primeiro${NC}"
    exit 1
fi

TOKEN=$(cat "$TOKEN_FILE")

echo -e "${BLUE}=========================================="
echo "  SGCA - Teste de Perfil de Usuário"
echo -e "==========================================${NC}\n"

# Teste 1: Criar usuário com dados pessoais e endereço completos
echo -e "${YELLOW}🧪 Teste 1: Criar usuário com dados pessoais e endereço${NC}"
CREATE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${API_ADMIN}/users" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{
        "nome": "João da Silva Teste",
        "cpf": "12345678901",
        "email": "joao.teste@example.com",
        "telefone": "(11) 98765-4321",
        "tipo": "ENFERMEIRO",
        "perfilIds": [2],
        "dadosPessoais": {
            "dataNascimento": "1990-05-15",
            "sexo": "MASCULINO",
            "genero": "CISGENERO",
            "rg": "123456789",
            "orgaoEmissor": "SSP-SP",
            "naturalidade": "São Paulo",
            "estadoCivil": "SOLTEIRO",
            "nomeMae": "Maria da Silva",
            "nomePai": "José da Silva",
            "profissao": "Enfermeiro"
        },
        "endereco": {
            "logradouro": "Rua das Flores",
            "numero": "123",
            "complemento": "Apto 45",
            "bairro": "Jardim Paulista",
            "cidade": "São Paulo",
            "uf": "SP",
            "cep": "01234-567"
        }
    }')

HTTP_STATUS=$(echo "$CREATE_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$CREATE_RESPONSE" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS" -eq 201 ]; then
    echo -e "${GREEN}✅ Usuário criado com sucesso!${NC}"
    echo -e "${BLUE}Resposta:${NC}"
    echo "$BODY" | jq '.'
    
    # Extrai o ID do usuário criado
    USER_ID=$(echo "$BODY" | jq -r '.id')
    echo -e "\n${GREEN}ID do usuário criado: ${USER_ID}${NC}"
else
    echo -e "${RED}❌ Erro ao criar usuário (Status: $HTTP_STATUS)${NC}"
    echo "$BODY" | jq '.'
    exit 1
fi

# Teste 2: Buscar usuário criado para verificar dados pessoais e endereço
echo -e "\n${YELLOW}🧪 Teste 2: Buscar usuário por ID${NC}"
GET_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "${API_ADMIN}/users/${USER_ID}" \
    -H "Authorization: Bearer ${TOKEN}")

HTTP_STATUS=$(echo "$GET_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$GET_RESPONSE" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS" -eq 200 ]; then
    echo -e "${GREEN}✅ Usuário encontrado!${NC}"
    echo -e "${BLUE}Resposta:${NC}"
    echo "$BODY" | jq '.'
else
    echo -e "${RED}❌ Erro ao buscar usuário (Status: $HTTP_STATUS)${NC}"
    echo "$BODY" | jq '.'
fi

# Teste 3: Atualizar dados pessoais e endereço
echo -e "\n${YELLOW}🧪 Teste 3: Atualizar dados pessoais e endereço${NC}"
UPDATE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X PUT "${API_ADMIN}/users/${USER_ID}" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{
        "nome": "João da Silva Atualizado",
        "telefone": "(11) 99999-8888",
        "dadosPessoais": {
            "dataNascimento": "1990-05-15",
            "sexo": "MASCULINO",
            "genero": "NAO_BINARIO",
            "rg": "987654321",
            "orgaoEmissor": "SSP-RJ",
            "naturalidade": "Rio de Janeiro",
            "estadoCivil": "CASADO",
            "nomeMae": "Maria da Silva Santos",
            "nomePai": "José da Silva Junior",
            "profissao": "Enfermeiro Chefe"
        },
        "endereco": {
            "logradouro": "Av. Paulista",
            "numero": "1000",
            "complemento": "Sala 10",
            "bairro": "Bela Vista",
            "cidade": "São Paulo",
            "uf": "SP",
            "cep": "01310-100"
        }
    }')

HTTP_STATUS=$(echo "$UPDATE_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$UPDATE_RESPONSE" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS" -eq 200 ]; then
    echo -e "${GREEN}✅ Usuário atualizado com sucesso!${NC}"
    echo -e "${BLUE}Resposta:${NC}"
    echo "$BODY" | jq '.'
else
    echo -e "${RED}❌ Erro ao atualizar usuário (Status: $HTTP_STATUS)${NC}"
    echo "$BODY" | jq '.'
fi

# Teste 4: Verificar endpoint /auth/me com dados pessoais e endereço
echo -e "\n${YELLOW}🧪 Teste 4: Verificar /auth/me (perfil próprio)${NC}"
ME_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "${BASE_URL}/auth/me" \
    -H "Authorization: Bearer ${TOKEN}")

HTTP_STATUS=$(echo "$ME_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$ME_RESPONSE" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS" -eq 200 ]; then
    echo -e "${GREEN}✅ Perfil obtido com sucesso!${NC}"
    echo -e "${BLUE}Resposta:${NC}"
    echo "$BODY" | jq '.'
else
    echo -e "${RED}❌ Erro ao obter perfil (Status: $HTTP_STATUS)${NC}"
    echo "$BODY" | jq '.'
fi

echo -e "\n${GREEN}=========================================="
echo "  ✅ Testes Concluídos!"
echo -e "==========================================${NC}"
