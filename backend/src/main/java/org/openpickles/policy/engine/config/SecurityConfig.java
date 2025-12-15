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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity in this demo
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**", "/favicon.ico", "/error").permitAll() // Allow H2 Console and
                                                                                                 // static assets
                        .requestMatchers("/api/v1/sync/**").permitAll() // Public sync API
                        .anyRequest().authenticated())
                .formLogin(withDefaults()) // Enable default login page
                .httpBasic(withDefaults()) // Enable Basic Auth for API testing
                .headers(headers -> headers.frameOptions(frame -> frame.disable())); // For H2 Console

        return http.build();
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
