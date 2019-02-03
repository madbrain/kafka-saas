package com.github.madbrain.apiserver.security;

import com.github.madbrain.apiserver.services.AuthenticationService;
import com.github.madbrain.apiserver.services.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER)) {

            String bearerToken = header.substring(BEARER.length());

            try {
                Jws<Claims> claims = jwtTokenService.validateJwtToken(bearerToken);
                Authentication authentication = authenticationService.getAuthentication(claims);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "error.jwt.expired");
                return;
            } catch (JwtException exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "error.jwt.invalid");
                return;
            }
        }
        filterChain.doFilter(request, response);
        SecurityContextHolder.getContext().setAuthentication(null); // Clean authentication after process
    }

}
