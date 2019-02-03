package com.github.madbrain.apiserver.services;

public class JwtTokens {
    private String token;

    public JwtTokens(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
