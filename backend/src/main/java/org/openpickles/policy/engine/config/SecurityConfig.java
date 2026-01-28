package org.openpickles.policy.engine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@Profile("prod")
public class SecurityConfig {

        private final AuthProperties authProperties;

        @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins}")
        private String allowedOrigins;

        public SecurityConfig(AuthProperties authProperties) {
                this.authProperties = authProperties;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/h2-console/**", "/favicon.ico", "/error").permitAll()
                                                // Require specific scope for registration/sync
                                                .requestMatchers("/api/v1/dist/**")
                                                .hasAuthority("SCOPE_policy.register")
                                                .requestMatchers("/ws/**").permitAll() // WebSocket might need its own
                                                                                       // auth strategy later
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                                .headers(headers -> headers.frameOptions(frame -> frame.disable())); // For H2 Console

                return http.build();
        }

        @Bean
        public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
                org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
                java.util.List<String> origins = java.util.Arrays.asList(allowedOrigins.split(","));
                configuration.setAllowedOrigins(origins);
                configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
