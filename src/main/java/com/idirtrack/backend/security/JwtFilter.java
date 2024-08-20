package com.idirtrack.backend.security;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.idirtrack.backend.jwt.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger Logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            logger.debug("Processing authentication for '{}'" + request.getRequestURL());

            // Check if the Token exist in the header of request
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String username;
            String path = request.getRequestURI();

            // Bypass JWT validation for public endpoints
            if (isPublicEndpoint(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Logger the auth header
            logger.info("Auth header: '{}'" + authHeader);
            // Logger the jwt
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            jwt = authHeader.substring(7);
            logger.info("JWT: '{}'" + jwt);

            username = jwtUtils.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Get the user details from the database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Logger the user details
                logger.info("User details: '{}'" + userDetails);

                // Validate the token
                if (jwtUtils.isTokenValid(jwt, userDetails)) {
                    // Create the authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    // Set the authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Set the authentication in the SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);

        }

        filterChain.doFilter(request, response);
    }


    private boolean isPublicEndpoint(String path) {
        return path.equals("/api/auth/register") || path.equals("/api/auth/login") || path.equals("/home");
    }
    

}
