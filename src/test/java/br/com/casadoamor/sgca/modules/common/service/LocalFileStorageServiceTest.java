package br.com.casadoamor.sgca.modules.common.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class LocalFileStorageServiceTest {

  @Test
  void salvarArquivo_and_delete_and_exist_and_url() throws Exception {
    LocalFileStorageService svc = new LocalFileStorageService();

    Path tmp = Files.createTempDirectory("localstore-test-");
    // set private fields
    java.lang.reflect.Field uploadDir = LocalFileStorageService.class.getDeclaredField("uploadDir");
    uploadDir.setAccessible(true);
    uploadDir.set(svc, tmp.toString());

    java.lang.reflect.Field baseUrl = LocalFileStorageService.class.getDeclaredField("baseUrl");
    baseUrl.setAccessible(true);
    baseUrl.set(svc, "http://files.test");

    MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", new ByteArrayInputStream("hello".getBytes()));

    String relative = svc.salvarArquivo(file, "avatars", null);
    assertThat(relative).startsWith("avatars/");

    Path saved = tmp.resolve(relative);
    assertThat(Files.exists(saved)).isTrue();

    assertThat(svc.arquivoExiste(relative)).isTrue();
    assertThat(svc.obterUrlPublica(relative)).isEqualTo("http://files.test/" + relative);

    boolean deleted = svc.deletarArquivo(relative);
    assertThat(deleted).isTrue();
    assertThat(svc.arquivoExiste(relative)).isFalse();

    // cleanup temp dir
    Files.walk(tmp).sorted(java.util.Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException e) {} });
  }

  @Test
  void salvarArquivo_empty_throws() throws Exception {
    LocalFileStorageService svc = new LocalFileStorageService();
    java.lang.reflect.Field uploadDir = LocalFileStorageService.class.getDeclaredField("uploadDir");
    uploadDir.setAccessible(true);
    Path tmp = Files.createTempDirectory("localstore-test-2-");
    uploadDir.set(svc, tmp.toString());

    MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

    assertThatThrownBy(() -> svc.salvarArquivo(file, "avatars", null)).isInstanceOf(IOException.class);

    Files.walk(tmp).sorted(java.util.Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException e) {} });
  }

  @Test
  void validarTipoETamanho() throws Exception {
    LocalFileStorageService svc = new LocalFileStorageService();

    MockMultipartFile file = new MockMultipartFile("file", "img.png", "image/png", new ByteArrayInputStream("data".getBytes()));

    assertThat(svc.validarTipoArquivo(file, new String[] {"image/png"})).isTrue();
    assertThat(svc.validarTipoArquivo(file, new String[] {"image/jpeg"})).isFalse();

    assertThat(svc.validarTamanhoArquivo(file, 1)).isTrue(); // size small

    MockMultipartFile big = new MockMultipartFile("big", "big.bin", "application/octet-stream", new byte[1024 * 1024 * 6]);
    assertThat(svc.validarTamanhoArquivo(big, 5)).isFalse();
  }
}
