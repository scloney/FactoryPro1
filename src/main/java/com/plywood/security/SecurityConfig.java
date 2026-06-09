package com.plywood.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ── Customer portal filter chain (handles /customer/**) ──────────────────
    @Bean
    @Order(1)
    public SecurityFilterChain customerFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/customer/**")
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/customer/register"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/customer/login", "/customer/register").permitAll()
                .anyRequest().hasRole("CUSTOMER")
            )
            .formLogin(form -> form
                .loginPage("/customer/login")
                .loginProcessingUrl("/customer/login")
                .defaultSuccessUrl("/customer/dashboard", true)
                .failureUrl("/customer/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/customer/logout")
                .logoutSuccessUrl("/customer/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        return http.build();
    }

    // ── Admin portal filter chain (handles everything else) ──────────────────
    @Bean
    @Order(2)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/optimizer/**"),
                    new AntPathRequestMatcher("/api/**")
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/api/*/send-whatsapp").permitAll()
                .requestMatchers("/api/barcode/**", "/barcode/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        return http.build();
    }
}
