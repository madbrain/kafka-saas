package com.github.madbrain.apiserver.services;

import org.springframework.util.StringUtils;

public class ResourcePath {

    private final String namespace;
    private final String resourceType;
    private final String resourceName;
    private final String subResource;

    public ResourcePath(String namespace, String resourceType, String resourceName, String subResource) {
        this.namespace = namespace;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.subResource = subResource;
    }

    public boolean notValid() {
        return StringUtils.isEmpty(resourceType);
    }

    public boolean allNamespaces() {
        return StringUtils.isEmpty(namespace);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getSubResource() {
        return subResource;
    }

    @Override
    public String toString() {
        return (namespace == null ? "" : "/namespaces/" + namespace) + "/" + resourceType
                + (resourceName == null ? "" : "/" + resourceName)
                + (subResource == null ? "" : "/" + subResource);
    }
}
