#!/bin/bash

# Script de teste: Ativação de conta com 2FA automático e rate limiting
# Testa o fluxo completo: criação → ativação → 2FA ativado → login com código

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Variáveis de ambiente
BASE_URL="${API_URL:-http://localhost:8080}"
TOKEN="${ADMIN_TOKEN:-}"

# Função para imprimir mensagens
print_step() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Verifica se jq está instalado
if ! command -v jq &> /dev/null; then
    print_error "jq não está instalado. Instale com: sudo apt-get install jq"
    exit 1
fi

# Gera CPF único
CPF=$(printf "%011d" $RANDOM)
EMAIL="teste2fa_${RANDOM}@example.com"
SENHA_TEMPORARIA="SenhaTemp@2024"
NOVA_SENHA="MinhaSenha@2024"

print_step "TESTE: ATIVAÇÃO COM 2FA AUTOMÁTICO E RATE LIMITING"
echo "CPF: $CPF"
echo "Email: $EMAIL"
echo ""

# ============================================
# PASSO 1: Criar usuário (precisa de token admin)
# ============================================
print_step "PASSO 1: Criando usuário"

if [ -z "$TOKEN" ]; then
    print_warning "Token admin não fornecido. Configure ADMIN_TOKEN no ambiente."
    print_warning "Execute: export ADMIN_TOKEN='seu_token_aqui'"
    exit 1
fi

CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/admin/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{
    "cpf": "'"$CPF"'",
    "nome": "Teste 2FA Automático",
    "email": "'"$EMAIL"'",
    "tipo": "PACIENTE"
  }')

echo "$CREATE_RESPONSE" | jq .

USUARIO_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id')

if [ "$USUARIO_ID" == "null" ] || [ -z "$USUARIO_ID" ]; then
    print_error "Falha ao criar usuário"
    exit 1
fi

print_success "Usuário criado com ID: $USUARIO_ID"
print_warning "Email de ativação enviado com senha temporária"
echo ""

# ============================================
# PASSO 2: Simular recebimento do email
# ============================================
print_step "PASSO 2: Verificando email de ativação (simulação)"
print_warning "Em produção, você receberia um email com:"
print_warning "  - Link de ativação com TOKEN"
print_warning "  - Senha temporária: $SENHA_TEMPORARIA"
echo ""
print_warning "IMPORTANTE: Você precisa pegar o TOKEN do banco de dados manualmente"
read -p "Digite o TOKEN de ativação recebido por email: " TOKEN_ATIVACAO
echo ""

# ============================================
# PASSO 3: Ativar conta (gera 2FA automaticamente)
# ============================================
print_step "PASSO 3: Ativando conta (2FA será ativado automaticamente)"

ACTIVATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/activate" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "'"$TOKEN_ATIVACAO"'",
    "senhaTemporaria": "'"$SENHA_TEMPORARIA"'",
    "novaSenha": "'"$NOVA_SENHA"'",
    "confirmacaoSenha": "'"$NOVA_SENHA"'"
  }')

echo "$ACTIVATE_RESPONSE" | jq .

ACTIVATE_MSG=$(echo "$ACTIVATE_RESPONSE" | jq -r '.mensagem')

if echo "$ACTIVATE_MSG" | grep -q "sucesso"; then
    print_success "Conta ativada com sucesso!"
    print_success "2FA foi ativado automaticamente!"
    print_warning "Código 2FA enviado para: $EMAIL"
else
    print_error "Falha na ativação: $ACTIVATE_MSG"
    exit 1
fi
echo ""

# ============================================
# PASSO 4: Tentar login (deve pedir código 2FA)
# ============================================
print_step "PASSO 4: Tentando login (deve solicitar código 2FA)"

LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "'"$CPF"'",
    "senha": "'"$NOVA_SENHA"'"
  }')

echo "$LOGIN_RESPONSE" | jq .

REQUIRES_2FA=$(echo "$LOGIN_RESPONSE" | jq -r '.requires2FA')

if [ "$REQUIRES_2FA" == "true" ]; then
    print_success "Login detectou 2FA habilitado! ✓"
    print_warning "Código enviado para: $EMAIL"
    USER_ID_LOGIN=$(echo "$LOGIN_RESPONSE" | jq -r '.userId')
    print_success "UserID para verificação: $USER_ID_LOGIN"
else
    print_error "2FA não foi solicitado no login!"
    exit 1
fi
echo ""

# ============================================
# PASSO 5: Verificar código 2FA
# ============================================
print_step "PASSO 5: Verificando código 2FA"
print_warning "Você precisa pegar o código do email (ou do banco de dados)"
read -p "Digite o código 2FA de 6 dígitos: " CODIGO_2FA
echo ""

VERIFY_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/2fa/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "'"$CPF"'",
    "codigo": "'"$CODIGO_2FA"'"
  }')

echo "$VERIFY_RESPONSE" | jq .

JWT_TOKEN=$(echo "$VERIFY_RESPONSE" | jq -r '.token')

if [ "$JWT_TOKEN" != "null" ] && [ -n "$JWT_TOKEN" ]; then
    print_success "LOGIN COMPLETO! Token JWT recebido! ✓"
    echo "Token: $JWT_TOKEN"
else
    print_error "Código 2FA inválido ou expirado"
    exit 1
fi
echo ""

# ============================================
# PASSO 6: Testar Rate Limiting
# ============================================
print_step "PASSO 6: Testando Rate Limiting (tentativas excessivas)"
print_warning "Tentando enviar códigos múltiplas vezes rapidamente..."
echo ""

for i in {1..5}; do
    echo "Tentativa $i de reenvio:"
    
    RESEND_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/2fa/resend" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer ${JWT_TOKEN}")
    
    echo "$RESEND_RESPONSE" | jq -r '.mensagem // .message // .'
    
    if echo "$RESEND_RESPONSE" | grep -q "Limite de envios excedido"; then
        print_success "Rate limiting funcionando! Bloqueio ativado na tentativa $i"
        break
    fi
    
    if [ $i -lt 5 ]; then
        echo "Aguardando 2 segundos..."
        sleep 2
    fi
done
echo ""

# ============================================
# RESUMO FINAL
# ============================================
print_step "RESUMO DO TESTE"
print_success "✓ Usuário criado"
print_success "✓ Conta ativada"
print_success "✓ 2FA ativado automaticamente na ativação"
print_success "✓ Login solicitou código 2FA"
print_success "✓ Verificação 2FA bem-sucedida"
print_success "✓ JWT recebido após 2FA"
print_success "✓ Rate limiting testado"
echo ""
echo -e "${GREEN}TESTE COMPLETO! 🎉${NC}"
echo ""
echo "Dados para testes adicionais:"
echo "  CPF: $CPF"
echo "  Email: $EMAIL"
echo "  Senha: $NOVA_SENHA"
echo "  Token JWT: ${JWT_TOKEN:0:50}..."
