package com.cloudflix.backend.security.jwt;

import com.cloudflix.backend.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Ensure this is a Spring component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = jwtUtils.parseJwt(request);
            // ---- START DETAILED LOGGING ----
            if (request.getRequestURI().contains("/api/videos")) { // Only log verbosely for relevant requests
                logger.info("AuthTokenFilter (/api/videos): JWT from request: {}", jwt);
            }
            // ---- END DETAILED LOGGING ----
            
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String email = jwtUtils.getEmailFromJwtToken(jwt);
                // ---- START DETAILED LOGGING ----
                if (request.getRequestURI().contains("/api/videos")) {
                     logger.info("AuthTokenFilter (/api/videos): Email from JWT: {}", email);
                }
                // ---- END DETAILED LOGGING ----

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                // ---- START DETAILED LOGGING ----
                if (request.getRequestURI().contains("/api/videos")) {
                    logger.info("AuthTokenFilter (/api/videos): UserDetails loaded: {}", userDetails.getUsername());
                    logger.info("AuthTokenFilter (/api/videos): User Authorities from UserDetails: {}", userDetails.getAuthorities());
                }
                // ---- END DETAILED LOGGING ----
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                                                null,
                                                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                // ---- START DETAILED LOGGING ----
                if (request.getRequestURI().contains("/api/videos")) {
                    logger.info("AuthTokenFilter (/api/videos): Authentication set in SecurityContext for user: {}. Authorities in Context: {}", email, SecurityContextHolder.getContext().getAuthentication().getAuthorities());
                }
                // ---- END DETAILED LOGGING ----
            } else {
                if (jwt == null && request.getRequestURI().contains("/api/videos")) {
                    logger.warn("AuthTokenFilter (/api/videos): JWT is null for request to {}", request.getRequestURI());
                } else if (jwt != null && request.getRequestURI().contains("/api/videos")) {
                    logger.warn("AuthTokenFilter (/api/videos): JWT validation failed for token: {}", jwt);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
            logger.error("AuthTokenFilter: Cannot set user authentication", e);
        }
        filterChain.doFilter(request, response);
    }
}