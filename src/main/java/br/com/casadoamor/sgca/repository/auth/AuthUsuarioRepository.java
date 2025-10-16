package br.com.casadoamor.sgca.repository.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.entity.auth.AuthUsuario;

@Repository
public interface AuthUsuarioRepository extends JpaRepository<AuthUsuario, Long> {
  Optional<AuthUsuario> findByCpf(String cpf);
  Optional<AuthUsuario> findByEmail(String email);
}
