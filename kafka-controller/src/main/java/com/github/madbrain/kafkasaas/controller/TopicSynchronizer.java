package com.github.madbrain.kafkasaas.controller;

import com.github.madbrain.kafkasaas.controller.api.Topic;
import com.github.madbrain.kafkasaas.controller.api.TopicList;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TopicSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerApplication.class);

    private AdminClient adminClient;
    private ApiProperties apiProperties;
    private RestTemplate restTemplate;

    @Autowired
    public TopicSynchronizer(RestTemplate restTemplate, AdminClient adminClient, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.adminClient = adminClient;
        this.apiProperties = apiProperties;
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void synchronize() {
        LOG.info("Synchronize topics");
        ResponseEntity<TopicList> response = restTemplate.exchange(
                apiProperties.getUrl() + "/api/v1/topics",
                HttpMethod.GET,
                new HttpEntity<>(RestUtils.createHeaders(apiProperties)),
                TopicList.class);
        List<Topic> expectedTopics = response.getBody().getItems();
        Set<String> existingNames = expectedTopics.stream()
                .map(t -> t.getSpec().getTopic())
                .collect(Collectors.toSet());
        Map<String, TopicListing> existingTopics = getExistingTopics()
                .stream().collect(Collectors.toMap(TopicListing::name, Function.identity()));

        List<Topic> toCreate = expectedTopics.stream()
                .filter(t -> !existingTopics.containsKey(t.getSpec().getTopic()))
                .peek(t -> {
                    LOG.info("topic " + t.getSpec().getTopic() + " to create in " + t.getMetadata().getNamespace());
                })
                .collect(Collectors.toList());
        Set<String> toDelete = existingTopics.keySet().stream()
                .filter(t -> !existingNames.contains(t))
                .peek(t -> {
                    LOG.info("topic " + t + " to delete ");
                })
                .collect(Collectors.toSet());

        deleteTopics(toDelete);
        createTopics(toCreate);
    }

    private Collection<TopicListing> getExistingTopics() {
        try {
            ListTopicsResult topics = adminClient.listTopics();
            return topics.listings().get();
        } catch (Exception e) {
            LOG.error("error while getting kafka topics", e);
        }
        return Collections.emptyList();
    }

    private void deleteTopics(Set<String> toDelete) {
        adminClient.deleteTopics(toDelete);
    }

    private void createTopics(Collection<Topic> topicsToCreate) {
        adminClient.createTopics(topicsToCreate.stream().map(this::createTopic).collect(Collectors.toList()));
    }

    private NewTopic createTopic(Topic topic) {
        // TODO add topic configuration
        return new NewTopic(
                topic.getSpec().getTopic(),
                topic.getSpec().getPartitions(),
                topic.getSpec().getReplicationFactor());
    }
}
