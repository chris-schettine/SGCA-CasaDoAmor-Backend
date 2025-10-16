# 🔒 Aviso de Segurança - SGCA Backend

## ⚠️ ATENÇÃO: Credenciais Expostas Foram Removidas

**Data:** 16 de outubro de 2025

### O que aconteceu?

Credenciais sensíveis foram temporariamente expostas em arquivos de documentação:
- Senha de App do Gmail
- Exemplos com credenciais reais em guias de configuração

### Ações Tomadas

✅ **Credenciais removidas** de todos os arquivos de documentação  
✅ **Placeholders genéricos** substituíram exemplos reais  
✅ **Arquivo .env** confirmado como não trackeado pelo git  
✅ **Templates seguros** criados (.env.template e .env.example)  
✅ **Avisos de segurança** adicionados em todos os guias  

### ⚠️ Ação Necessária URGENTE

Se você é o proprietário da conta **casadoamoremconquista@gmail.com**:

1. **REVOGUE IMEDIATAMENTE** a senha de app antiga
2. **GERE UMA NOVA** senha de app
3. **ATUALIZE** seu arquivo `.env` local
4. **REINICIE** os containers Docker

#### Como Revogar e Gerar Nova Senha de App

```bash
# 1. Acesse sua conta Google
# https://myaccount.google.com/apppasswords

# 2. Localize "Senhas de app"

# 3. Revogue a senha antiga (se aparecer "SGCA Backend" ou similar)

# 4. Gere uma nova senha de app

# 5. Atualize seu .env local (NÃO COMMITE!)
nano .env

# 6. Reinicie os containers
docker compose down
docker compose up -d
```

## 🛡️ Melhores Práticas de Segurança

### Nunca Commit Credenciais

❌ **NUNCA faça:**
- Commit de arquivos `.env`
- Commit de senhas ou tokens
- Push de credenciais em documentação
- Compartilhe senhas de app publicamente

✅ **SEMPRE faça:**
- Use templates de exemplo (`.env.example`, `.env.template`)
- Mantenha `.env` no `.gitignore`
- Use placeholders em documentação (`sua-senha-aqui`, `****************`)
- Revogue credenciais expostas imediatamente

### Verificar Antes de Commit

```bash
# Sempre verifique o que será commitado
git status
git diff

# Nunca use 'git add .' sem revisar
# Prefira adicionar arquivos específicos
git add arquivo-seguro.md
```

### Arquivo .gitignore

Certifique-se que seu `.gitignore` contém:

```gitignore
# Environment variables (CRITICAL!)
.env
.env.local
.env.*.local
*.env

# Credentials and secrets
secrets/
credentials/
*.key
*.pem
*.p12
```

## 🔍 Como Verificar Se Suas Credenciais Estão Expostas

### 1. Verificar Histórico do Git

```bash
# Procurar por senha no histórico
git log -S "senha" --all

# Verificar se .env foi commitado
git log --all --full-history -- .env
```

### 2. Verificar Arquivos Staged

```bash
# Ver o que está prestes a ser commitado
git diff --cached

# Listar arquivos trackeados
git ls-files
```

### 3. Verificar GitHub/GitLab

- Acesse o repositório online
- Verifique se arquivos `.env` aparecem
- Procure por credenciais em Issues/Pull Requests

## 📚 Recursos Adicionais

### Ferramentas para Remover Credenciais do Histórico

Se credenciais foram commitadas:

```bash
# Usando git-filter-repo (recomendado)
git filter-repo --path .env --invert-paths

# Ou usando BFG Repo-Cleaner
bfg --delete-files .env
```

⚠️ **ATENÇÃO**: Essas ferramentas reescrevem o histórico do git!

### Links Úteis

- [GitHub: Remover dados sensíveis](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)
- [Google App Passwords](https://support.google.com/accounts/answer/185833)
- [OWASP: Credential Management](https://cheatsheetseries.owasp.org/cheatsheets/Credential_Management_Cheat_Sheet.html)

## 📝 Checklist de Segurança

Antes de fazer push:

- [ ] `.env` está no `.gitignore`?
- [ ] Nenhuma senha aparece em `git diff`?
- [ ] Documentação usa placeholders genéricos?
- [ ] Templates estão sem credenciais reais?
- [ ] Revisei todos os arquivos modificados?

## 🆘 Suporte

Se você acidentalmente expôs credenciais:

1. **NÃO ENTRE EM PÂNICO**
2. Revogue as credenciais imediatamente
3. Gere novas credenciais
4. Remova do histórico do git (se necessário)
5. Aprenda com o erro e implemente verificações

---

**Lembre-se:** Segurança é responsabilidade de todos! 🔒

**Última atualização:** 16 de outubro de 2025
