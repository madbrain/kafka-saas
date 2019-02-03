package com.github.madbrain.apiserver.services.impl;

import com.github.madbrain.apiserver.services.JwtTokenService;
import com.github.madbrain.apiserver.services.JwtTokens;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    @Value("${token.secret}")
    private String secret;

    @Override
    public JwtTokens createTokens(Authentication authentication) {
        UserDetails user = (UserDetails) authentication.getPrincipal();

        String token = createToken(user);

        return new JwtTokens(token);
    }

    @Override
    public Jws<Claims> validateJwtToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
    }

    private String createToken(UserDetails user) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, secret)
                .setClaims(buildUserClaims(user))
                .setExpiration(getTokenExpirationDate(false))
                .setIssuedAt(new Date())
                .compact();
    }

    private Claims buildUserClaims(UserDetails user) {
        Claims claims = new DefaultClaims();

        claims.setSubject(String.valueOf(user.getUsername()));
        claims.put("username", user.getUsername());
//        claims.put("email", user.getEmail());
        claims.put("roles", String.join(",", AuthorityUtils.authorityListToSet(user.getAuthorities())));

        return claims;
    }

    private Date getTokenExpirationDate(boolean refreshToken) {
        Calendar calendar = Calendar.getInstance();

        if(refreshToken) {
            calendar.add(Calendar.MONTH, 1);
        } else {
            calendar.add(Calendar.MINUTE, 5);
        }
        return calendar.getTime();
    }
}
