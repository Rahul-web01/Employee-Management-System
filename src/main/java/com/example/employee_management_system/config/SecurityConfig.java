package com.example.employee_management_system.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Who can open which page.
 *
 * <ul>
 *   <li>Everyone must sign in first (except the login page itself).</li>
 *   <li>VIEWER, HR and ADMIN can see and search the employee list.</li>
 *   <li>HR and ADMIN can add and edit employees.</li>
 *   <li>Only ADMIN can delete employees.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // internal forwards to the error / access-denied pages
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                .requestMatchers("/login", "/access-denied", "/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/employees/delete/**").hasRole("ADMIN")
                .requestMatchers("/employees/new", "/employees/edit/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.POST, "/employees/save").hasAnyRole("ADMIN", "HR")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/employees")
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));

        return http.build();
    }

    /** Stores passwords as BCrypt hashes, e.g. "{bcrypt}$2a$10$...". */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
