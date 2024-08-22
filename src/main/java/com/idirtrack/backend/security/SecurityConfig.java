package com.idirtrack.backend.security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties.Authentication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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
                    .requestMatchers("/user-api/auth/login").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // Endpoints for ADMIN
                    .requestMatchers("/user-api/admin/**").hasAuthority("ADMIN")
                    /**
                     * Endpoints for manager APIs, only users with the role ADMIN can access this
                     */
                    .requestMatchers("/user-api/manager/**").hasAuthority("ADMIN")
                    .requestMatchers("/api/**").hasAnyAuthority("ADMIN", "MANAGER")

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
