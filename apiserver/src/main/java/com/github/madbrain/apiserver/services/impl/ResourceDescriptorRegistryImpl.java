package com.github.madbrain.apiserver.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.madbrain.apiserver.api.*;
import com.github.madbrain.apiserver.services.ResourceDescriptor;
import com.github.madbrain.apiserver.services.ResourceDescriptorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class ResourceDescriptorRegistryImpl implements ResourceDescriptorRegistry, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceDescriptorRegistryImpl.class);

    private final Map<String, ResourceDescriptor> resourceDescriptors = new HashMap<>();

    @Value("${schema.path}")
    private String schemaPath;

    @Autowired
    private ObjectMapper objectMapper;

    private void add(ResourceDescriptor descriptor) {
        this.resourceDescriptors.put(descriptor.getResourceType(), descriptor);
    }

    @Override
    public ResourceDescriptor get(String resourceType) {
        return resourceDescriptors.get(resourceType);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.json");
        Files.walk(Paths.get(schemaPath))
                .filter(file -> Files.isRegularFile(file) && matcher.matches(file.getFileName()))
                .forEach(path -> {
                    LOGGER.info("Loading schema {}", path);
                    try {
                        final JsonNode schemaNode = objectMapper.readTree(path.toFile());
                        String resourceType = schemaNode.get("resourceType").asText();
                        String listKind = schemaNode.get("listKind").asText();
                        boolean useNamespace = schemaNode.get("useNamespace").asBoolean();
                        final JsonSchema schema = factory.getJsonSchema(schemaNode.get("schema"));
                        LOGGER.info("Descriptor for {} (use namespace: {})", resourceType, useNamespace);
                        add(new ResourceDescriptor(resourceType, listKind, ApiObject.class, ApiList.class, useNamespace, schema));
                    } catch (IOException | ProcessingException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });

        add(new ResourceDescriptor("namespaces", Namespace.class, NamespaceList.class, false, readSchema(factory, objectMapper, "/schemas/namespaces.json")));
        add(new ResourceDescriptor("roles", Role.class, RoleList.class, true, readSchema(factory, objectMapper, "/schemas/roles.json")));
        add(new ResourceDescriptor("rolebindings", RoleBinding.class, RoleBindingList.class, true, readSchema(factory, objectMapper, "/schemas/rolebindings.json")));
        add(new ResourceDescriptor("clusterroles", ClusterRole.class, ClusterRoleList.class, false, readSchema(factory, objectMapper, "/schemas/clusterroles.json")));
        add(new ResourceDescriptor("clusterrolebindings", ClusterRoleBinding.class, ClusterRoleBindingList.class, false, readSchema(factory, objectMapper, "/schemas/clusterrolebindings.json")));
        add(new ResourceDescriptor("serviceaccounts", ServiceAccount.class, ServiceAccountList.class, true, readSchema(factory, objectMapper, "/schemas/serviceaccounts.json")));
        add(new ResourceDescriptor("secrets", Secret.class, SecretList.class, true, readSchema(factory, objectMapper, "/schemas/secrets.json")));
    }

    private static JsonSchema readSchema(JsonSchemaFactory factory, ObjectMapper mapper, String path) throws IOException, ProcessingException {
        return factory.getJsonSchema(mapper.readTree(ResourceDescriptorRegistryImpl.class.getResourceAsStream(path)));
    }
}
