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
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
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
                    new AntPathRequestMatcher("/api/**"),
                    // Many controllers expose REST endpoints under /<feature>/api/**
                    // (e.g. /purchase-orders/api, /inventory/api, /suppliers/api,
                    // /sales-orders/api, /bills/api). These are called via raw
                    // fetch() without a CSRF token, so they must be exempted too.
                    new AntPathRequestMatcher("/*/api/**"),
                    // /bill/generate-pdf is called via raw fetch() (no CSRF token)
                    // from bill.html, unlike /api/quotations/generate-pdf which is
                    // already covered by /api/** above.
                    new AntPathRequestMatcher("/bill/generate-pdf")
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/api/*/send-whatsapp").permitAll()
                // Only ADMIN can register new users
                .requestMatchers("/register").hasRole("ADMIN")
                // Barcode features need authentication
                .requestMatchers("/api/barcode/**", "/barcode/**").authenticated()
                // STAFF can access all operational areas
                .requestMatchers(
                    "/", "/bills/**", "/quotations-list", "/quotation/**",
                    "/sales-orders/**", "/inventory/**", "/customers/**",
                    "/suppliers/**", "/purchase-orders/**", "/optimizer/**",
                    "/inventory/reports", "/inventory/export/**"
                ).hasAnyRole("ADMIN", "STAFF")
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