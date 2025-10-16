#!/bin/bash

# Script para testar o envio de emails
# Certifique-se de que a aplicação está rodando em http://localhost:8090

echo "=========================================="
echo "  TESTE DE ENVIO DE EMAIL - SGCA BACKEND"
echo "=========================================="
echo ""

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# URL base da API
BASE_URL="http://localhost:8090"

# Função para testar recuperação de senha
test_forgot_password() {
    echo -e "${YELLOW}Testando: Esqueci minha senha (Forgot Password)${NC}"
    echo "Endpoint: POST /auth/forgot-password"
    echo ""
    
    read -p "Digite o email para teste (exemplo: seu@email.com): " TEST_EMAIL
    
    echo ""
    echo "Enviando requisição..."
    
    RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${BASE_URL}/auth/forgot-password" \
      -H "Content-Type: application/json" \
      -d "{\"email\": \"${TEST_EMAIL}\"}")
    
    HTTP_BODY=$(echo "$RESPONSE" | sed -e 's/HTTP_STATUS\:.*//g')
    HTTP_STATUS=$(echo "$RESPONSE" | tr -d '\n' | sed -e 's/.*HTTP_STATUS://')
    
    echo ""
    echo "Status HTTP: ${HTTP_STATUS}"
    echo "Resposta: ${HTTP_BODY}"
    echo ""
    
    if [ "$HTTP_STATUS" -eq 200 ] || [ "$HTTP_STATUS" -eq 201 ]; then
        echo -e "${GREEN}✓ Requisição enviada com sucesso!${NC}"
        echo -e "${GREEN}✓ Verifique a caixa de entrada do email: ${TEST_EMAIL}${NC}"
        echo ""
        echo "Verifique também os logs da aplicação para confirmar o envio:"
        echo "  docker logs spring_sgca 2>&1 | grep -i mail"
    else
        echo -e "${RED}✗ Erro ao enviar requisição${NC}"
        echo "Verifique se:"
        echo "  1. A aplicação está rodando (docker ps)"
        echo "  2. O email existe no banco de dados"
        echo "  3. As configurações de email estão corretas"
    fi
    
    echo ""
}

# Função para verificar logs de email
check_email_logs() {
    echo -e "${YELLOW}Verificando logs de email na aplicação...${NC}"
    echo ""
    
    docker logs spring_sgca 2>&1 | grep -i -E "(mail|smtp|email)" | tail -n 20
    
    echo ""
}

# Função para verificar variáveis de ambiente
check_env_vars() {
    echo -e "${YELLOW}Verificando variáveis de ambiente de email...${NC}"
    echo ""
    
    docker exec spring_sgca env | grep -E "(MAIL|SGCA_EMAIL)" | sort
    
    echo ""
}

# Menu principal
while true; do
    echo ""
    echo "Escolha uma opção:"
    echo "1) Testar envio de email (Forgot Password)"
    echo "2) Verificar variáveis de ambiente"
    echo "3) Verificar logs de email"
    echo "4) Verificar se aplicação está rodando"
    echo "5) Abrir Swagger UI no navegador"
    echo "0) Sair"
    echo ""
    read -p "Opção: " choice
    
    case $choice in
        1)
            test_forgot_password
            ;;
        2)
            check_env_vars
            ;;
        3)
            check_email_logs
            ;;
        4)
            echo "Verificando containers Docker..."
            docker ps | grep -E "(CONTAINER|sgca)"
            echo ""
            echo "Testando conexão com a API..."
            curl -s "${BASE_URL}/actuator/health" | python3 -m json.tool || echo "Aplicação não está respondendo"
            ;;
        5)
            echo "Abrindo Swagger UI..."
            xdg-open "${BASE_URL}/swagger-ui/index.html" 2>/dev/null || \
            open "${BASE_URL}/swagger-ui/index.html" 2>/dev/null || \
            echo "Acesse manualmente: ${BASE_URL}/swagger-ui/index.html"
            ;;
        0)
            echo "Saindo..."
            exit 0
            ;;
        *)
            echo -e "${RED}Opção inválida!${NC}"
            ;;
    esac
done
