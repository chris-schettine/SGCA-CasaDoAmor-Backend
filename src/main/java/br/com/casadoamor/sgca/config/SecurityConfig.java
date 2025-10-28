package br.com.casadoamor.sgca.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.com.casadoamor.sgca.security.JwtAuthenticationFilter;
import br.com.casadoamor.sgca.security.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita @PreAuthorize, @Secured, etc.
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt com força 12 (recomendado para dados sensíveis de saúde)
        // Força 10 = padrão, 12 = mais seguro para healthcare
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configura o AuthenticationProvider com nosso UserDetailsService
     * Usa constructor injection (Spring Security 6.x best practice)
     * 
     * @param userDetailsService - serviço customizado para carregar usuários
     * @return DaoAuthenticationProvider configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expõe o AuthenticationManager como bean (necessário para login)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, 
                                                    AuthenticationProvider authenticationProvider) throws Exception {
        http
                // Disable CSRF for REST API (use JWT tokens instead)
                .csrf(csrf -> csrf.disable())

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Session management - stateless for REST API
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - authentication não requerida
                        .requestMatchers(
                                "/docs/**", // Swagger UI
                                "/v3/api-docs/**", // OpenAPI docs
                                "/swagger-ui/**", // Swagger resources
                                "/actuator/health", // Health check
                                "/auth/login", // Login endpoint
                                "/auth/register", // Register endpoint
                                "/auth/forgot-password", // Recuperação de senha
                                "/auth/reset-password", // Reset de senha
                                "/auth/verify-email", // Verificação de email
                                "/auth/activate-account", // Ativação de conta (usuário não está autenticado)
                                "/auth/resend-activation", // Reenvio de email de ativação
                                "/api/files/**" // Servir arquivos estáticos (fotos)
                        ).permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated())

                // Configura o authentication provider (injetado via parâmetro)
                .authenticationProvider(authenticationProvider)

                // Adiciona o filtro JWT antes do UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Reference to CORS configuration from CorsConfig
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite todas as origens em desenvolvimento
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
