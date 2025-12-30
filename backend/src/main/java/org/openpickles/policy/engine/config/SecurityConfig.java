package org.openpickles.policy.engine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.security.web.authentication.AuthenticationSuccessHandler successHandler;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.security.web.authentication.AuthenticationFailureHandler failureHandler;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.security.web.authentication.logout.LogoutSuccessHandler logoutSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity in this demo
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**", "/favicon.ico", "/error").permitAll() // Allow H2 Console and
                                                                                                 // static assets
                        .requestMatchers("/api/v1/sync/**").permitAll() // Public sync API
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .permitAll())
                .httpBasic(withDefaults()) // Enable Basic Auth for API testing
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

    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        org.springframework.security.core.userdetails.UserDetails user = org.springframework.security.core.userdetails.User
                .withDefaultPasswordEncoder()
                .username("admin")
                .password("admin123")
                .roles("ADMIN")
                .build();

        return new org.springframework.security.provisioning.InMemoryUserDetailsManager(user);
    }
}
