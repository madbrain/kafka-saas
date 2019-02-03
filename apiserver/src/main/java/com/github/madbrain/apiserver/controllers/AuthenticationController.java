package com.github.madbrain.apiserver.controllers;


import com.github.madbrain.apiserver.services.AuthenticationRequest;
import com.github.madbrain.apiserver.services.AuthenticationService;
import com.github.madbrain.apiserver.services.JwtTokenService;
import com.github.madbrain.apiserver.services.JwtTokens;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWT Authentication method from http://blog.ippon.fr/2017/10/12/preuve-dauthentification-avec-jwt/
 */
@RestController
@RequestMapping("/api")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @PostMapping(value = "/auth", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity authenticate(@RequestBody AuthenticationRequest authenticationRequest) {

        Authentication authentication = authenticationService.authenticate(authenticationRequest);

        if (authentication != null && authentication.isAuthenticated()) {
            JwtTokens tokens = jwtTokenService.createTokens(authentication);
            return ResponseEntity.ok().body(tokens);
        }
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }

}
