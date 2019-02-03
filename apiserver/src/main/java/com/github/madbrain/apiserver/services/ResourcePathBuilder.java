package com.github.madbrain.apiserver.services;

import org.springframework.util.StringUtils;

public class ResourcePathBuilder {

    public static ResourcePath build(String resourcePath) {
        int index = 0;
        String[] pathElements = resourcePath.split("/");
        String namespace = null;
        String resourceType = null;
        String resourceName = null;
        String subResource = null;
        if (pathElements.length >= 2 && pathElements[0].equals("namespaces")) {
            namespace = pathElements[1];
            index += 2;
        }
        if (!StringUtils.isEmpty(namespace) && pathElements.length <= index) {
            return new ResourcePath(null, "namespaces", namespace, null);
        }
        if (pathElements.length > index) {
            resourceType = pathElements[index++];
        }
        if (pathElements.length > index) {
            resourceName = pathElements[index++];
        }
        if (pathElements.length > index) {
            subResource = pathElements[index];
        }
        return new ResourcePath(namespace, resourceType, resourceName, subResource);
    }
}
