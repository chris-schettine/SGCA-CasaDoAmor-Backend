package br.com.casadoamor.sgca.modules.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;
import br.com.casadoamor.sgca.modules.auth.repository.AuthUsuarioRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller temporário para debug de autenticação
 * REMOVER EM PRODUÇÃO!
 */
@RestController
@RequestMapping("/debug-auth")
public class TestAuthController {

    @Autowired
    private AuthUsuarioRepository authUsuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Testa a senha contra o hash do banco
     */
    @GetMapping("/test-password")
    public ResponseEntity<Map<String, Object>> testPassword(
            @RequestParam String cpf,
            @RequestParam String senha) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Busca o usuário
            AuthUsuario usuario = authUsuarioRepository.findByCpf(cpf)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            // Verifica a senha
            boolean matches = passwordEncoder.matches(senha, usuario.getSenhaHash());
            
            response.put("cpf", cpf);
            response.put("usuarioEncontrado", true);
            response.put("usuarioNome", usuario.getNome());
            response.put("usuarioAtivo", usuario.getAtivo());
            response.put("senhaMatches", matches);
            response.put("hashNoBanco", usuario.getSenhaHash());
            
        } catch (Exception e) {
            response.put("erro", e.getMessage());
            response.put("usuarioEncontrado", false);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos os usuários
     */
    @GetMapping("/list-users")
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        
        for (AuthUsuario usuario : authUsuarioRepository.findAll()) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", usuario.getId());
            userMap.put("cpf", usuario.getCpf());
            userMap.put("nome", usuario.getNome());
            userMap.put("email", usuario.getEmail());
            userMap.put("tipo", usuario.getTipo());
            userMap.put("ativo", usuario.getAtivo());
            users.add(userMap);
        }
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/generate-hash")
    public ResponseEntity<Map<String, Object>> generateHash(@RequestParam String senha) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String hash = passwordEncoder.encode(senha);
            boolean matches = passwordEncoder.matches(senha, hash);
            
            response.put("senha", senha);
            response.put("hash", hash);
            response.put("selfCheck", matches);
            response.put("message", "Hash BCrypt gerado com sucesso (força 12)");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("erro", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
