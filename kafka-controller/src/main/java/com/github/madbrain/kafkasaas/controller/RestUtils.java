package com.github.madbrain.kafkasaas.controller;

import org.springframework.http.HttpHeaders;

import java.nio.charset.Charset;
import java.util.Base64;

public class RestUtils {

    public static HttpHeaders createHeaders(ApiProperties apiProperties) {
        return new HttpHeaders() {{
            String auth = apiProperties.getUsername() + ":" + apiProperties.getPassword();
            byte[] encodedAuth = Base64.getEncoder().encode(
                    auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }
}
