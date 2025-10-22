# 🎫 Token Generator Script - Usage Guide

## 📋 Quick Start

### Generate a token with default admin credentials:
```bash
./get-token.sh
```

**Default credentials:**
- CPF: `00000000000`
- Password: `Admin@123`

---

## 🔧 Advanced Usage

### Generate token with custom credentials:
```bash
./get-token.sh <CPF> <PASSWORD> [API_URL]
```

**Examples:**

```bash
# Different user
./get-token.sh 12345678901 MyPassword@123

# Different API URL (for production)
./get-token.sh 00000000000 Admin@123 https://api.production.com

# Just different password
./get-token.sh 00000000000 NewPassword@456
```

---

## 📤 Output

The script will:
1. ✅ Display login confirmation
2. 👤 Show user name and type
3. ⏱️ Show token expiration time
4. 🎫 Display the JWT token
5. 📋 Provide instructions for Swagger and curl
6. 💾 Save token to `/tmp/sgca_token.txt`

---

## 🔄 Using the Generated Token

### In Swagger UI:
1. Open: http://localhost:8090/swagger-ui/index.html
2. Click **"Authorize"** button (🔒)
3. Paste the token (without quotes)
4. Click **"Authorize"**
5. Test protected endpoints!

### In curl:
```bash
# Read token from file
TOKEN=$(cat /tmp/sgca_token.txt)

# Use in requests
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8090/api/admin/users
```

### In Postman:
1. Go to **Authorization** tab
2. Select **Type**: Bearer Token
3. Paste the token in **Token** field

---

## 📝 Token Information

- **Expiration**: 60 minutes (1 hour)
- **Algorithm**: HS512
- **Storage**: `/tmp/sgca_token.txt`

---

## 💡 Tips

### Get just the token (no formatting):
```bash
./get-token.sh | grep -A1 "SEU TOKEN JWT:" | tail -1
```

### Save to environment variable:
```bash
export TOKEN=$(./get-token.sh | grep -A1 "SEU TOKEN JWT:" | tail -1)
echo $TOKEN
```

### One-liner for quick testing:
```bash
curl -H "Authorization: Bearer $(cat /tmp/sgca_token.txt)" \
  http://localhost:8090/api/admin/users
```

---

## ❌ Troubleshooting

### Error: "Credenciais inválidas"
- Check if CPF and password are correct
- Verify the user exists in database
- Make sure the application is running

### Error: "Connection refused"
- Verify the application is running: `docker ps`
- Check if the port is correct (default: 8090)
- Try: `curl http://localhost:8090/actuator/health`

### Token expired
- Just run `./get-token.sh` again to get a fresh token
- Tokens expire after 60 minutes

---

## 🔐 Security Notes

⚠️ **IMPORTANT**: 
- Never commit tokens to git
- Don't share tokens publicly
- Rotate passwords regularly in production
- Use environment variables for credentials in production

---

**Created**: October 21, 2025  
**Version**: 1.0
