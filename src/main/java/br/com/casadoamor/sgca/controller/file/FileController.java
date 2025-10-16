package br.com.casadoamor.sgca.controller.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.casadoamor.sgca.service.file.FileStorageService;

import java.nio.file.Path;

/**
 * Controller para servir arquivos armazenados
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * Serve um arquivo armazenado
     * GET /api/files/avatars/usuario-123.jpg
     */
    @GetMapping("/{subDirectory}/{fileName:.+}")
    public ResponseEntity<Resource> servirArquivo(
            @PathVariable String subDirectory,
            @PathVariable String fileName) {
        
        try {
            String filePath = subDirectory + "/" + fileName;
            
            if (!fileStorageService.arquivoExiste(filePath)) {
                log.warn("Arquivo não encontrado: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            Path path = fileStorageService.obterCaminhoAbsoluto(filePath);
            Resource resource = new UrlResource(path.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                log.error("Arquivo não pode ser lido: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // Determina o tipo de conteúdo
            String contentType = determinarContentType(fileName);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Erro ao servir arquivo: {}/{}", subDirectory, fileName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Determina o Content-Type baseado na extensão do arquivo
     */
    private String determinarContentType(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFileName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFileName.endsWith(".pdf")) {
            return "application/pdf";
        }
        
        return "application/octet-stream";
    }
}
