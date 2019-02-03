package com.github.madbrain.apiserver.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.security.core.Authentication;

public interface JwtTokenService {
    JwtTokens createTokens(Authentication authentication);

    Jws<Claims> validateJwtToken(String bearerToken);
}
