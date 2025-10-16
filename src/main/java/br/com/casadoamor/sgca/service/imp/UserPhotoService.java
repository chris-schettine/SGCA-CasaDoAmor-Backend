package br.com.casadoamor.sgca.service.imp;

import br.com.casadoamor.sgca.entity.auth.AuthUsuario;
import br.com.casadoamor.sgca.repository.auth.AuthUsuarioRepository;
import br.com.casadoamor.sgca.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Serviço para gerenciar fotos de perfil dos usuários
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserPhotoService {

    private final FileStorageService fileStorageService;
    private final AuthUsuarioRepository authUsuarioRepository;

    private static final String[] TIPOS_PERMITIDOS = {
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    };
    
    private static final long TAMANHO_MAXIMO_MB = 5; // 5 MB
    private static final String SUBDIRETORIO_AVATARES = "avatars";

    /**
     * Faz upload da foto de perfil do usuário
     */
    @Transactional
    public String uploadFoto(Long usuarioId, MultipartFile file) throws IOException {
        // Valida o usuário
        AuthUsuario usuario = authUsuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Valida o arquivo
        validarArquivo(file);

        // Deleta foto antiga se existir
        if (usuario.getFotoPath() != null) {
            fileStorageService.deletarArquivo(usuario.getFotoPath());
            log.info("Foto antiga deletada: {}", usuario.getFotoPath());
        }

        // Gera nome único para o arquivo
        String nomeArquivo = gerarNomeArquivo(usuarioId, file);

        // Salva o arquivo
        String relativePath = fileStorageService.salvarArquivo(file, SUBDIRETORIO_AVATARES, nomeArquivo);

        // Atualiza o usuário no banco
        String fotoUrl = fileStorageService.obterUrlPublica(relativePath);
        usuario.setFotoPath(relativePath);
        usuario.setFotoUrl(fotoUrl);
        usuario.setFotoAtualizadaEm(LocalDateTime.now());
        authUsuarioRepository.save(usuario);

        log.info("Foto de perfil atualizada para usuário ID: {}", usuarioId);
        return fotoUrl;
    }

    /**
     * Deleta a foto de perfil do usuário
     */
    @Transactional
    public void deletarFoto(Long usuarioId) {
        AuthUsuario usuario = authUsuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getFotoPath() != null) {
            fileStorageService.deletarArquivo(usuario.getFotoPath());
            usuario.setFotoPath(null);
            usuario.setFotoUrl(null);
            usuario.setFotoAtualizadaEm(null);
            authUsuarioRepository.save(usuario);
            log.info("Foto de perfil removida para usuário ID: {}", usuarioId);
        }
    }

    /**
     * Obtém a URL da foto do usuário
     */
    public String obterUrlFoto(Long usuarioId) {
        AuthUsuario usuario = authUsuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        return usuario.getFotoUrl();
    }

    /**
     * Valida o arquivo enviado
     */
    private void validarArquivo(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Arquivo vazio");
        }

        if (!fileStorageService.validarTipoArquivo(file, TIPOS_PERMITIDOS)) {
            throw new IOException("Tipo de arquivo não permitido. Permitidos: JPEG, PNG, GIF, WEBP");
        }

        if (!fileStorageService.validarTamanhoArquivo(file, TAMANHO_MAXIMO_MB)) {
            throw new IOException("Arquivo muito grande. Tamanho máximo: " + TAMANHO_MAXIMO_MB + " MB");
        }
    }

    /**
     * Gera nome único para o arquivo
     */
    private String gerarNomeArquivo(Long usuarioId, MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        return "usuario-" + usuarioId + "-" + System.currentTimeMillis() + extension;
    }
}
