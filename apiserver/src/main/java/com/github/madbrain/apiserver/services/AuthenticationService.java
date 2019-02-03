package com.github.madbrain.apiserver.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.security.core.Authentication;

public interface AuthenticationService {

    Authentication authenticate(AuthenticationRequest authenticationRequest);

    Authentication getAuthentication(Jws<Claims> token);
}
