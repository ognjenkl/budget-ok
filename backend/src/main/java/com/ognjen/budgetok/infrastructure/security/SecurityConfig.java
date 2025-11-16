package com.ognjen.budgetok.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    String loginPath = "/login";
    String homePath = "/home";

    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers(
                loginPath, 
                "/error",
                "/css/**",
                "/js/**",
                "/webjars/**",
                "api/envelopes/**",
                "/api/echo"
            ).permitAll()
            // Actuator endpoints (read-only access)
            .requestMatchers("/actuator/health/**").permitAll()
            .requestMatchers("/actuator/info").permitAll()
            .requestMatchers("/actuator/env").permitAll()
            .requestMatchers("/actuator").permitAll()
            // API security
            .requestMatchers("/api/test/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/test/**").authenticated()
            .requestMatchers("/api/**").authenticated()
            // Web pages and API endpoints
            .requestMatchers("/home").authenticated()
            // Envelope API endpoints
            .requestMatchers("/api/envelopes/**").authenticated()
            .requestMatchers("/actuator/**").hasRole("ADMIN") // Secure other actuator endpoints
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage(loginPath)
            .defaultSuccessUrl(homePath, true)
            .permitAll()
        )
        .logout(logout -> logout
            .permitAll()
            .logoutSuccessUrl(loginPath)
        );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
