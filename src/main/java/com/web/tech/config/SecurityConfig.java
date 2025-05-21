package com.web.tech.config;

import com.web.tech.filter.AuthFilter;
import com.web.tech.filter.LoginPageFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final UserDetailsService userDetailsService;
    private final AuthFilter authFilter;
    private final CustomLogoutHandler customLogoutHandler;
    private final LoginPageFilter loginPageFilter;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService,
                          AuthFilter authFilter,
                          CustomLogoutHandler customLogoutHandler,
                          LoginPageFilter loginPageFilter) {
        this.userDetailsService = userDetailsService;
        this.authFilter = authFilter;
        this.customLogoutHandler = customLogoutHandler;
        this.loginPageFilter = loginPageFilter;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Add filters in correct order
                .addFilterBefore(loginPageFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)

                // Disable caching for all responses
                .headers(headers -> headers
                        .cacheControl(cache -> cache.disable())
                        .frameOptions(frame -> frame.sameOrigin())
                )

                // Configure session management
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .sessionRegistry(sessionRegistry())
                )

                // Disable CSRF for custom login
                .csrf(csrf -> csrf.disable())

                // Authorization configuration
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/css/**", "/js/**", "/images/**",
                                "/landing", "/register", "/login", "/exhibitions",
                                "/collection", "/error/**", "/test", "/forgot-password", "/reset-password"
                        ).permitAll()
                        .requestMatchers("/admin/products/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/user/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .anyRequest().authenticated()
                )

                // Disable form login
                .formLogin(form -> form.disable())

                // Logout configuration
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .addLogoutHandler(customLogoutHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )

                // Exception handling
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                );

        logger.debug("SecurityFilterChain configured with AuthFilter and LoginPageFilter");
        return http.build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Using NoOpPasswordEncoder as requested (not recommended for production)
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public Java8TimeDialect java8TimeDialect() {
        return new Java8TimeDialect();
    }
}