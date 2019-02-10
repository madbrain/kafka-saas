package com.github.madbrain.apiserver.services.impl;

import com.github.madbrain.apiserver.api.Secret;
import com.github.madbrain.apiserver.api.ServiceAccount;
import com.github.madbrain.apiserver.services.AuthenticationRequest;
import com.github.madbrain.apiserver.services.AuthenticationService;
import com.github.madbrain.apiserver.services.ObjectStore;
import com.github.madbrain.apiserver.services.ResourceDescriptorRegistry;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthenticationServiceImpl implements AuthenticationService  {

    private final AuthenticationManager authenticationManager;
    private final ObjectStore objectStore;
    private final ResourceDescriptorRegistry registry;

    @Autowired
    public AuthenticationServiceImpl(AuthenticationManager authenticationManager,
                                     ObjectStore objectStore,
                                     ResourceDescriptorRegistry registry) {
        this.authenticationManager = authenticationManager;
        this.objectStore = objectStore;
        this.registry = registry;
    }

    @Override
    public Authentication authenticate(AuthenticationRequest authenticationRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                authenticationRequest.getUsername(), authenticationRequest.getPassword());
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    public Authentication getAuthentication(String tokenData, Jws<Claims> token) {
        Claims claims = token.getBody();
        if (!StringUtils.isEmpty(claims.getIssuer())
                && claims.getIssuer().equals("serviceaccount")) {
            // Validate further service account tokens as they don't expire
            String namespace = claims.get("Namespace", String.class);
            String secretName = claims.get("SecretName", String.class);
            String serviceAccountName = claims.get("ServiceAccountName", String.class);

            if (StringUtils.isEmpty(namespace)
                    || StringUtils.isEmpty(secretName)
                    || StringUtils.isEmpty(serviceAccountName)) {
                return null;
            }
            Secret secret = objectStore.get(namespace, secretName, registry.get("secrets"), Secret.class);
            if (secret == null || ! tokenData.equals(secret.getData())) {
                return null;
            }
            ServiceAccount serviceAccount = objectStore.get(namespace, serviceAccountName, registry.get("serviceaccounts"), ServiceAccount.class);
            if (serviceAccount == null) {
                return null;
            }
        }
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "PROTECTED",
                AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get("roles", String.class)));
    }
}
