package com.github.madbrain.apiserver.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.madbrain.apiserver.api.ApiObject;
import com.github.madbrain.apiserver.api.Status;
import com.github.madbrain.apiserver.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/**")
public class ApiResources {

    @Autowired
    private ResourceDescriptorRegistry registry;

    @Autowired
    private ObjectStore store;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getResource(HttpServletRequest request) throws URISyntaxException {
        ResourcePath resourcePath = ResourcePathBuilder.build(extractPathFromPattern(request));
        if (resourcePath.notValid()) {
            return ResponseEntity.badRequest()
                    .body(Status.of("resource type required"));
        }
        ResourceDescriptor descriptor = registry.get(resourcePath.getResourceType());
        if (descriptor == null) {
            return ResponseEntity.badRequest()
                    .body(Status.of("unknown resource type '" + resourcePath.getResourceType() + "'"));
        }
        if (!descriptor.useNamespace() && !StringUtils.isEmpty(resourcePath.getNamespace())) {
            return ResponseEntity.notFound().location(new URI(request.getRequestURI())).build();
        }
        if (StringUtils.isEmpty(resourcePath.getResourceName())) {
            return ResponseEntity.ok(store.getRawAll(resourcePath.getNamespace(), descriptor));
        }
        return ResponseEntity.ok(store.getRaw(resourcePath.getNamespace(), resourcePath.getResourceName(), descriptor));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity postResource(HttpServletRequest request, HttpEntity<String> httpEntity) throws IOException, URISyntaxException {
        ResourcePath resourcePath = ResourcePathBuilder.build(extractPathFromPattern(request));
        if (resourcePath.notValid()) {
            return ResponseEntity.badRequest()
                    .body(Status.of("resource type required"));
        }
        ResourceDescriptor descriptor = registry.get(resourcePath.getResourceType());
        if (descriptor == null) {
            return ResponseEntity.badRequest()
                    .body(Status.of("unknown resource type '" + resourcePath.getResourceType() + "'"));
        }

        if (!StringUtils.isEmpty(resourcePath.getResourceName())) {
            return ResponseEntity.badRequest()
                    .body(Status.of("resource name not allowed"));
        }
        JsonNode node = objectMapper.readTree(httpEntity.getBody());
        ApiObject object = objectMapper.treeToValue(node, ApiObject.class);
        String name = ApiUtils.getName(object);
        if (StringUtils.isEmpty(name)) {
            return ResponseEntity.badRequest()
                    .body(Status.of("required meta.name"));
        }
        String namespace = null;
        if (descriptor.useNamespace()) {
            if (StringUtils.isEmpty(resourcePath.getNamespace())) {
                return ResponseEntity.badRequest()
                        .body(Status.of("namespace required"));
            }
            namespace = ApiUtils.getNamespace(object);
            if (namespace != null && !resourcePath.getNamespace().equals(namespace)) {
                return ResponseEntity.badRequest()
                        .body(Status.of("namespaces don't match"));
            }
            object.getMetadata().setNamespace(resourcePath.getNamespace());
        } else {
            if (!StringUtils.isEmpty(resourcePath.getNamespace())) {
                return ResponseEntity.badRequest()
                        .body(Status.of("namespace not allowed"));
            }
            object.getMetadata().setNamespace(null);
        }
        ValidationResult validationResult = validate(node, descriptor);
        if (!validationResult.isValid) {
            // TODO send better validation errors
            return ResponseEntity.badRequest()
                    .body(Status.of("object doesn't conform to schema: " + validationResult.message));
        }
        ApiObject existingObject = store.get(namespace, name, descriptor, descriptor.getResourceClass());
        if (existingObject != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Status.of("'" + name + "' already exists"));
        }
        store.put(object, node, descriptor);
        return ResponseEntity.created(new URI("/api/v1/" + resourcePath.toString())).build();
    }

    private ValidationResult validate(JsonNode node, ResourceDescriptor descriptor) throws IOException {
        try {
            ProcessingReport report = descriptor.getSchema().validate(node);
            if (!report.isSuccess()) {
                String message = StreamSupport.stream(report.spliterator(), false)
                        .map(ProcessingMessage::toString)
                        .collect(Collectors.joining("\n"));
                return ValidationResult.error(message);
            }
        } catch (ProcessingException e) {
            return ValidationResult.error(e.getMessage());
        }
        return ValidationResult.ok();
    }

    private static class ValidationResult {

        private boolean isValid = true;
        private String message;

        public static ValidationResult error(String message) {
            ValidationResult validationResult = new ValidationResult();
            validationResult.isValid = false;
            validationResult.message = message;
            return validationResult;
        }

        public static ValidationResult ok() {
            return new ValidationResult();
        }
    }

    @PutMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity replaceResource(HttpServletRequest request, HttpEntity<String> httpEntity) throws IOException, URISyntaxException {
        ResourcePath resourcePath = ResourcePathBuilder.build(extractPathFromPattern(request));
        if (resourcePath.notValid()) {
            return ResponseEntity.badRequest()
                    .body(Status.of("resource type required"));
        }
        ResourceDescriptor descriptor = registry.get(resourcePath.getResourceType());
        if (descriptor == null) {
            return ResponseEntity.badRequest()
                    .body(Status.of("unknown resource type '" + resourcePath.getResourceType() + "'"));
        }
        if (StringUtils.isEmpty(resourcePath.getResourceName())) {
            return ResponseEntity.badRequest()
                    .body(Status.of("resource name required"));
        }
        JsonNode node = objectMapper.readTree(httpEntity.getBody());
        ApiObject object = objectMapper.treeToValue(node, ApiObject.class);
        String name = ApiUtils.getName(object);
        if (StringUtils.isEmpty(name)) {
            return ResponseEntity.badRequest()
                    .body(Status.of("required meta.name"));
        }
        if (!resourcePath.getResourceName().equals(name)) {
            return ResponseEntity.badRequest()
                    .body(Status.of("names don't match"));
        }
        if (descriptor.useNamespace()) {
            if (StringUtils.isEmpty(resourcePath.getNamespace())) {
                return ResponseEntity.badRequest()
                        .body(Status.of("namespace required"));
            }
            String namespace = ApiUtils.getNamespace(object);
            if (namespace != null && !resourcePath.getNamespace().equals(namespace)) {
                return ResponseEntity.badRequest()
                        .body(Status.of("namespaces don't match"));
            }
            object.getMetadata().setNamespace(resourcePath.getNamespace());
        } else {
            if (!StringUtils.isEmpty(resourcePath.getNamespace())) {
                return ResponseEntity.badRequest()
                        .body(Status.of("namespace not allowed"));
            }
        }
        ValidationResult validationResult = validate(node, descriptor);
        if (!validationResult.isValid) {
            return ResponseEntity.badRequest()
                    .body(Status.of("object doesn't conform to schema: " + validationResult.message));
        }
        store.put(object, node, descriptor);
        return ResponseEntity.created(new URI("/api/v1/" + resourcePath.toString())).build();
    }

    @DeleteMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity deleteResource(HttpServletRequest request) {
        ResourcePath resourcePath = ResourcePathBuilder.build(extractPathFromPattern(request));
        if (resourcePath.notValid()) {
            return ResponseEntity.badRequest()
                    .body(Status.of("resource type required"));
        }
        ResourceDescriptor descriptor = registry.get(resourcePath.getResourceType());
        if (descriptor == null) {
            return ResponseEntity.badRequest()
                    .body(Status.of("unknown resource type '" + resourcePath.getResourceType() + "'"));
        }
        if (StringUtils.isEmpty(resourcePath.getResourceName())) {
            return ResponseEntity.badRequest()
                    .body(Status.of("resource name required"));
        }
        store.delete(resourcePath.getNamespace(), resourcePath.getResourceName(), descriptor);
        return ResponseEntity.ok().build();
    }

    private static String extractPathFromPattern(final HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }

    @PatchMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity patchResource(HttpServletRequest request, HttpEntity<String> httpEntity) throws IOException, JsonPatchException {
        ResourcePath resourcePath = ResourcePathBuilder.build(extractPathFromPattern(request));
        if (resourcePath.notValid()) {
            return ResponseEntity.badRequest()
                    .body(Status.of("resource type required"));
        }
        ResourceDescriptor descriptor = registry.get(resourcePath.getResourceType());
        if (descriptor == null) {
            return ResponseEntity.badRequest()
                    .body(Status.of("unknown resource type '" + resourcePath.getResourceType() + "'"));
        }
        if (StringUtils.isEmpty(resourcePath.getResourceName())) {
            return ResponseEntity.badRequest()
                    .body(Status.of("resource name required"));
        }
        JsonNode source = store.getRaw(resourcePath.getNamespace(), resourcePath.getResourceName(), descriptor);
        if (source == null) {
            return ResponseEntity.badRequest()
                    .body(Status.of("unknown object " + resourcePath.toString()));
        }
        JsonPatch patch = objectMapper.readValue(httpEntity.getBody(), JsonPatch.class);
        JsonNode result = patch.apply(source);
        ValidationResult validationResult = validate(result, descriptor);
        if (!validationResult.isValid) {
            return ResponseEntity.badRequest()
                    .body(Status.of("object doesn't conform to schema: " + validationResult.message));
        }
        ApiObject object = objectMapper.treeToValue(result, ApiObject.class);
        store.put(object, result, descriptor);
        return ResponseEntity.ok().build();
    }

}
