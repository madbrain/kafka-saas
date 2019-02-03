package com.github.madbrain.apiserver.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.madbrain.apiserver.api.ApiList;
import com.github.madbrain.apiserver.api.ApiObject;

public interface ObjectStore {
    <T extends ApiObject> T get(String namespace, String name, ResourceDescriptor descriptor, Class<T> cls);

    JsonNode getRaw(String namespace, String name, ResourceDescriptor descriptor);

    void put(ApiObject object, JsonNode node, ResourceDescriptor descriptor);

    <T extends ApiList> T getAll(String namespace, ResourceDescriptor descriptor, Class<T> cls);

    JsonNode getRawAll(String namespace, ResourceDescriptor descriptor);

    void delete(String namespace, String name, ResourceDescriptor descriptor);
}
