package br.com.casadoamor.sgca.infra.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class CpfUtilTest {

    @Test
    void limparCpf_null_returnsNull() {
        assertThat(CpfUtil.limparCpf(null)).isNull();
    }

    @Test
    void limparCpf_formatsCorrectly() {
        assertThat(CpfUtil.limparCpf("123.456.789-00")).isEqualTo("12345678900");
        assertThat(CpfUtil.limparCpf(" 123 456 789 00 ")).isEqualTo("12345678900");
        assertThat(CpfUtil.limparCpf("abc123.456")).isEqualTo("123456");
    }

    @Test
    void limparTelefone_null_and_formats() {
        assertThat(CpfUtil.limparTelefone(null)).isNull();
        assertThat(CpfUtil.limparTelefone("(11) 98765-4321")).isEqualTo("11987654321");
        assertThat(CpfUtil.limparTelefone("+55 (11) 99777-1234")).isEqualTo("5511997771234");
    }
}
