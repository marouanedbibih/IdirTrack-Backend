package com.idirtrack.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtFilter jwtFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Disable CSRF
        http.csrf(AbstractHttpConfigurer::disable);
        // Authorize requests based on the URL and the role of the user
        http.authorizeHttpRequests(request -> {
            // ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)
            // request.anyRequest()).authenticated();
            request
                    // Public endpoints
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Endpoints for manage managers
                    .requestMatchers("/api/v1/manager/**").hasAuthority("ADMIN")
                    .requestMatchers("/api/v1/managers/**").hasAuthority("ADMIN")
                    // Endpoints for manage clients
                    .requestMatchers("/api/v1/client/**").hasAnyAuthority("ADMIN", "MANAGER")
                    .requestMatchers("/api/v1/clients/**").hasAnyAuthority("ADMIN", "MANAGER")
                    // Endpoints for manage staff
                    .requestMatchers("/api/v1/staff/**").hasAnyAuthority("ADMIN", "MANAGER")
                    .requestMatchers("/api/v1/staffs/**").hasAnyAuthority("ADMIN", "MANAGER")
                    // Endpoints for manage devices
                    .requestMatchers("/api/device/**").hasAnyAuthority("ADMIN", "MANAGER")
                    .requestMatchers("/api/devices/**").hasAnyAuthority("ADMIN", "MANAGER")
                    // Endppoint to manage sims
                    .requestMatchers("/api/sim/**").hasAnyAuthority("ADMIN", "MANAGER")
                    .requestMatchers("/api/sims/**").hasAnyAuthority("ADMIN", "MANAGER")
                    // Endpoint to manage boitiers
                    .requestMatchers("/api/boitier/**").hasAnyAuthority("ADMIN", "MANAGER")
                    .requestMatchers("/api/boitiers/**").hasAnyAuthority("ADMIN", "MANAGER")
                    // Endpoint to manage vehicles
                    .requestMatchers("/api/vehicle/**").hasAnyAuthority("ADMIN", "MANAGER")
                    .requestMatchers("/api/vehicles/**").hasAnyAuthority("ADMIN", "MANAGER")
                    // Endpoint to manage subscriptions
                    .requestMatchers("/api/v1/subscriptions/**").hasAnyAuthority("ADMIN", "MANAGER")
                  

                    .anyRequest().authenticated();
        });
        // Set the session management to stateless
        http.sessionManagement(
                sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Set the authentication provider
        http.authenticationProvider(authenticationProvider());

        http.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler));

        // Add the JWT filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // http.httpBasic(Customizer.withDefaults());
        return (SecurityFilterChain) http.build();
    }

    // Password encoder bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication provider bean
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Define the DaoAuthenticationProvider
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        // Set the custom user details service
        provider.setUserDetailsService(customUserDetailsService);
        // Set the password encoder
        provider.setPasswordEncoder(passwordEncoder());

        // Return the provider
        return provider;
    }

    // Configure the AuthenticationManager Bean
    // @Bean
    // public void configure(AuthenticationManagerBuilder auth) throws Exception {
    // auth.authenticationProvider(authenticationProvider());
    // }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
