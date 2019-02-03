package com.github.madbrain.apiserver.services;

public interface ResourceDescriptorRegistry {
    ResourceDescriptor get(String resourceType);
}
