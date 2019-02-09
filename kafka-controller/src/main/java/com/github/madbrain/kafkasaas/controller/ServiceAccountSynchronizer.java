package com.github.madbrain.kafkasaas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.madbrain.apiserver.api.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

@Service
public class ServiceAccountSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerApplication.class);

    private static final String SERVICE_ACCOUNT_ISSUER = "serviceaccount";

    private ApiProperties apiProperties;
    private String secretKey;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @Autowired
    public ServiceAccountSynchronizer(RestTemplate restTemplate,
                                      ApiProperties apiProperties,
                                      ObjectMapper objectMapper,
                                      @Value("${token.secret}") String secretKey) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
        this.secretKey = secretKey;
        this.objectMapper = objectMapper;
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void synchronize() {
        LOG.debug("Synchronize service account");
        ResponseEntity<ServiceAccountList> response = restTemplate.exchange(
                apiProperties.getUrl() + "/api/v1/serviceaccounts",
                HttpMethod.GET,
                new HttpEntity<>(RestUtils.createHeaders(apiProperties)),
                ServiceAccountList.class);

        response.getBody().getItems().forEach(this::ensureSecret);
    }

    private void ensureSecret(ServiceAccount serviceAccount) {
        if (!hasReferencedToken(serviceAccount)) {
            LOG.info("create secret for service account " + serviceAccount.getMetadata().getName()
                    + " in " + serviceAccount.getMetadata().getNamespace());

            Secret secret = new Secret();
            secret.setMetadata(new ObjectMeta());
            secret.getMetadata().setName(NameUtils.generateName(serviceAccount.getMetadata().getName() + "-token-"));
            secret.getMetadata().setNamespace(serviceAccount.getMetadata().getNamespace());
            secret.getMetadata().setAnnotations(new HashMap<>());
            secret.getMetadata().getAnnotations().put("ServiceAccountName", serviceAccount.getMetadata().getName());
            secret.setType("SecretTypeServiceAccountToken");
            secret.setData(generateToken(serviceAccount, secret));

            ResponseEntity<Secret> responseCreate = restTemplate.exchange(
                    apiProperties.getUrl() + "/api/v1/namespaces/" + serviceAccount.getMetadata().getNamespace() + "/secrets",
                    HttpMethod.POST,
                    new HttpEntity<>(secret, RestUtils.createHeaders(apiProperties)),
                    Secret.class);

            try {
                JsonPatch patch = new JsonPatch(Collections.singletonList(
                        createPatchOperation(serviceAccount, secret)));
                ResponseEntity<Status> responseUpdate = restTemplate.exchange(
                        apiProperties.getUrl()
                                + "/api/v1/namespaces/" + serviceAccount.getMetadata().getNamespace()
                                + "/serviceaccounts/" + serviceAccount.getMetadata().getName(),
                        HttpMethod.PATCH,
                        new HttpEntity<>(patch, RestUtils.createHeaders(apiProperties)),
                        Status.class);
            } catch (JsonPointerException e) {
                throw new RuntimeException(e);
            }

            // TODO verify responses and remove or retry on error
        }
    }

    private JsonPatchOperation createPatchOperation(ServiceAccount serviceAccount, Secret secret) throws JsonPointerException {
        if (serviceAccount.getSecrets() != null) {
            return new AddOperation(
                    new JsonPointer("/secrets/-"),
                    objectMapper.valueToTree(ObjectReference.from(secret)));
        } else {
            return new AddOperation(
                    new JsonPointer("/secrets"),
                    objectMapper.valueToTree(Collections.singletonList(ObjectReference.from(secret))));
        }
    }

    private String generateToken(ServiceAccount serviceAccount, Secret secret) {
        Claims claims = new DefaultClaims();
        claims.setIssuer(SERVICE_ACCOUNT_ISSUER);
        claims.setSubject(serviceAccount.getUsername());
        claims.put("Namespace", serviceAccount.getMetadata().getNamespace());
        claims.put("ServiceAccountName", serviceAccount.getMetadata().getName());
        claims.put("SecretName", secret.getMetadata().getName());
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .compact();
    }

    private boolean hasReferencedToken(ServiceAccount serviceAccount) {
        ResponseEntity<SecretList> response = restTemplate.exchange(
                apiProperties.getUrl() + "/api/v1/namespaces/" + serviceAccount.getMetadata().getNamespace() + "/secrets",
                HttpMethod.GET,
                new HttpEntity<>(RestUtils.createHeaders(apiProperties)),
                SecretList.class);
        return response.getBody().getItems().stream()
                .anyMatch(secret -> secret.getMetadata().getAnnotations().get("ServiceAccountName").equals(serviceAccount.getMetadata().getName())
                        && serviceAccount.getSecrets() != null
                        && serviceAccount.getSecrets().stream().anyMatch(ref -> ref.getName().equals(secret.getMetadata().getName())));
    }

}
