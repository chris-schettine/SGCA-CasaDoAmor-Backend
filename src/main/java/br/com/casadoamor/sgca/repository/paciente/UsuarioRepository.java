package br.com.casadoamor.sgca.repository.paciente;

import br.com.casadoamor.sgca.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
}
