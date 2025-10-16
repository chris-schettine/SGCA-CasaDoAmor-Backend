package br.com.casadoamor.sgca.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface para serviços de armazenamento de arquivos.
 * Permite diferentes implementações (local, S3, Azure Blob, etc.)
 */
public interface FileStorageService {
    
    /**
     * Salva um arquivo no storage
     * @param file Arquivo a ser salvo
     * @param subDirectory Subdiretório onde o arquivo será salvo (ex: "avatars", "documentos")
     * @param fileName Nome do arquivo (opcional, se null gera automaticamente)
     * @return Caminho relativo do arquivo salvo
     * @throws IOException Se houver erro ao salvar o arquivo
     */
    String salvarArquivo(MultipartFile file, String subDirectory, String fileName) throws IOException;
    
    /**
     * Deleta um arquivo do storage
     * @param filePath Caminho relativo do arquivo
     * @return true se deletado com sucesso, false caso contrário
     */
    boolean deletarArquivo(String filePath);
    
    /**
     * Obtém o caminho absoluto de um arquivo
     * @param filePath Caminho relativo do arquivo
     * @return Path absoluto do arquivo
     */
    Path obterCaminhoAbsoluto(String filePath);
    
    /**
     * Verifica se um arquivo existe
     * @param filePath Caminho relativo do arquivo
     * @return true se o arquivo existe, false caso contrário
     */
    boolean arquivoExiste(String filePath);
    
    /**
     * Obtém a URL pública para acessar o arquivo
     * @param filePath Caminho relativo do arquivo
     * @return URL completa para acessar o arquivo
     */
    String obterUrlPublica(String filePath);
    
    /**
     * Valida se o tipo de arquivo é permitido
     * @param file Arquivo a ser validado
     * @param tiposPermitidos Array de tipos MIME permitidos
     * @return true se o tipo é permitido, false caso contrário
     */
    boolean validarTipoArquivo(MultipartFile file, String[] tiposPermitidos);
    
    /**
     * Valida o tamanho do arquivo
     * @param file Arquivo a ser validado
     * @param tamanhoMaximoMB Tamanho máximo em MB
     * @return true se o tamanho é válido, false caso contrário
     */
    boolean validarTamanhoArquivo(MultipartFile file, long tamanhoMaximoMB);
}
