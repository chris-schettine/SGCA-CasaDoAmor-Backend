#!/bin/bash
TOKEN=$(curl -s -X POST "http://localhost:8090/auth/login" -H "Content-Type: application/json" -d '{"cpf":"00000000000","senha":"Admin@123"}' | jq -r '.token')

curl -s -X POST "http://localhost:8090/admin/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"nome":"Dr. Carlos Medeiros","email":"dr.carlos@casadoamor.com","cpf":"77788899900","telefone":"(77) 99999-8888","tipo":"MEDICO","registroProfissional":{"tipoProfissional":"MEDICO","numeroRegistro":"789456-SP","rqe":"98765"}}' | jq '.'
