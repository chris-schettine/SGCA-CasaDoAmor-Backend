package br.com.casadoamor.sgca;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Substitui o seed de dados em testes para evitar dependência de migrations.
 */
@Configuration
@Profile("test")
public class TestDataSeederOverrideConfig {

    @Bean(name = "seedDatabase")
    CommandLineRunner seedDatabaseOverride() {
        return args -> {
            // Não faz nada; evita que o DataSeeder real rode durante os testes.
        };
    }
}
