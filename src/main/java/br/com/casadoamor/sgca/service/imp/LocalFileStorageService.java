package br.com.casadoamor.sgca.service.imp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.casadoamor.sgca.service.file.FileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementação local do serviço de armazenamento de arquivos.
 * Armazena arquivos no sistema de arquivos local (ou volume Docker).
 */
@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.upload.base-url:http://localhost:8080/api/files}")
    private String baseUrl;

    @Override
    public String salvarArquivo(MultipartFile file, String subDirectory, String fileName) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Arquivo vazio não pode ser salvo");
        }

        // Cria o diretório se não existir
        Path uploadPath = Paths.get(uploadDir, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Gera nome do arquivo se não fornecido
        if (fileName == null || fileName.isBlank()) {
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName != null && originalFileName.contains(".")
                    ? originalFileName.substring(originalFileName.lastIndexOf("."))
                    : "";
            fileName = UUID.randomUUID().toString() + extension;
        }

        // Salva o arquivo
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Retorna o caminho relativo
        String relativePath = subDirectory + "/" + fileName;
        log.info("Arquivo salvo com sucesso: {}", relativePath);
        
        return relativePath;
    }

    @Override
    public boolean deletarArquivo(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("Arquivo deletado com sucesso: {}", filePath);
            } else {
                log.warn("Arquivo não encontrado para deleção: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("Erro ao deletar arquivo: {}", filePath, e);
            return false;
        }
    }

    @Override
    public Path obterCaminhoAbsoluto(String filePath) {
        return Paths.get(uploadDir, filePath);
    }

    @Override
    public boolean arquivoExiste(String filePath) {
        Path path = obterCaminhoAbsoluto(filePath);
        return Files.exists(path);
    }

    @Override
    public String obterUrlPublica(String filePath) {
        return baseUrl + "/" + filePath;
    }

    @Override
    public boolean validarTipoArquivo(MultipartFile file, String[] tiposPermitidos) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        for (String tipo : tiposPermitidos) {
            if (contentType.equalsIgnoreCase(tipo)) {
                return true;
            }
        }
        
        log.warn("Tipo de arquivo não permitido: {}", contentType);
        return false;
    }

    @Override
    public boolean validarTamanhoArquivo(MultipartFile file, long tamanhoMaximoMB) {
        long tamanhoMaximoBytes = tamanhoMaximoMB * 1024 * 1024;
        long tamanhoArquivo = file.getSize();
        
        if (tamanhoArquivo > tamanhoMaximoBytes) {
            log.warn("Arquivo muito grande: {} bytes (máximo: {} MB)", tamanhoArquivo, tamanhoMaximoMB);
            return false;
        }
        
        return true;
    }
}
