package com.github.madbrain.apiserver.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.madbrain.apiserver.api.ApiBase;
import com.github.madbrain.apiserver.api.ApiList;
import com.github.madbrain.apiserver.api.ApiObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

public class ResourceDescriptor {
    private final String resourceType;
    private final Class<? extends ApiObject> resourceClass;
    private final Class<? extends ApiList> resourceListClass;
    private boolean useNamespace;
    private JsonSchema schema;
    private String listKind;

    public ResourceDescriptor(String resourceType,
                              String listKind, Class<? extends ApiObject> resourceClass,
                              Class<? extends ApiList> resourceListClass,
                              boolean useNamespace, JsonSchema schema) {

        this.resourceType = resourceType;
        this.listKind = listKind;
        this.resourceClass = resourceClass;
        this.resourceListClass = resourceListClass;
        this.useNamespace = useNamespace;
        this.schema = schema;
    }

    public ResourceDescriptor(String resourceType,
                              Class<? extends ApiObject> resourceClass,
                              Class<? extends ApiList> resourceListClass,
                              boolean useNamespace, JsonSchema schema) {

        this(resourceType, getVersion(resourceListClass), resourceClass, resourceListClass, useNamespace, schema);
    }

    private static String getVersion(Class<? extends ApiList> resourceListClass) {
        try {
            ApiList list = resourceListClass.getConstructor(List.class).newInstance(Collections.emptyList());
            return list.getKind() + ":" + list.getApiVersion();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public ApiBase buildList(List<ApiObject> objects) {
        try {
            Constructor<? extends ApiBase> constructor = resourceListClass.getConstructor(List.class);
            return constructor.newInstance(objects);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<? extends ApiObject> getResourceClass() {
        return resourceClass;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Class<? extends ApiList> getResourceListClass() {
        return resourceListClass;
    }

    public boolean useNamespace() {
        return useNamespace;
    }

    public JsonSchema getSchema() {
        return schema;
    }

    public String getListKind() {
        return listKind;
    }

}
