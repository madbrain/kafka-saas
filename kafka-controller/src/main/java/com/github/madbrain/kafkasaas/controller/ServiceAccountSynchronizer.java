package com.github.madbrain.kafkasaas.controller;

import com.github.madbrain.apiserver.api.*;
import org.apache.kafka.clients.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
public class ServiceAccountSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerApplication.class);

    private ApiProperties apiProperties;
    private RestTemplate restTemplate;

    @Autowired
    public ServiceAccountSynchronizer(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void synchronize() {
        LOG.info("Synchronize service account");
        ResponseEntity<ServiceAccountList> response = restTemplate.exchange(
                apiProperties.getUrl() + "/api/v1/serviceaccounts",
                HttpMethod.GET,
                new HttpEntity<>(RestUtils.createHeaders(apiProperties)),
                ServiceAccountList.class);

        response.getBody().getItems().forEach(this::ensureSecret);

        // TODO verify that secrets are correctly associated with an account if not delete secrets
    }

    private void ensureSecret(ServiceAccount serviceAccount) {
        if (! hasReferencedToken(serviceAccount)) {
            LOG.info("create secret for service account " + serviceAccount.getMetadata().getName()
                    + " in " + serviceAccount.getMetadata().getNamespace());

            Secret secret = new Secret();
            secret.setMetadata(new ObjectMeta());
            secret.getMetadata().setName(generateName(serviceAccount.getMetadata().getName() + "%s-token-"));
            secret.getMetadata().setNamespace(serviceAccount.getMetadata().getNamespace());
            secret.getMetadata().setAnnotations(new HashMap<>());
            secret.getMetadata().getAnnotations().put("ServiceAccountName", serviceAccount.getMetadata().getName());
            secret.setType("SecretTypeServiceAccountToken");
            secret.setData(generateToken(serviceAccount));
            // TODO save secret
            // TODO add reference to secret into serviceAccount
        }

    }

    private String generateToken(ServiceAccount serviceAccount) {
        throw new RuntimeException("TODO generate token for service account");
    }

    private String generateName(String stemplateName) {
        throw new RuntimeException("TODO generate unique name");
    }

    private boolean hasReferencedToken(ServiceAccount serviceAccount) {
        ResponseEntity<SecretList> response = restTemplate.exchange(
                apiProperties.getUrl() + "/api/v1/namespaces/" + serviceAccount.getMetadata().getNamespace() + "/secrets",
                HttpMethod.GET,
                new HttpEntity<>(RestUtils.createHeaders(apiProperties)),
                SecretList.class);
        return response.getBody().getItems().stream()
                .anyMatch(secret -> secret.getMetadata().getAnnotations().get("ServiceAccountName").equals(serviceAccount.getMetadata().getName())
                && serviceAccount.getSecrets().stream().anyMatch(ref -> ref.getName().equals(secret.getMetadata().getName())));
    }

}
