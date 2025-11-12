package br.com.casadoamor.sgca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
class SgcaBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
