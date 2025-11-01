package br.com.casadoamor.sgca.modules.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.casadoamor.sgca.modules.auth.entity.AuthUsuario;

@Repository
public interface AuthUsuarioRepository extends JpaRepository<AuthUsuario, Long>, JpaSpecificationExecutor<AuthUsuario> {
  Optional<AuthUsuario> findByCpf(String cpf);
  Optional<AuthUsuario> findByEmail(String email);
}
