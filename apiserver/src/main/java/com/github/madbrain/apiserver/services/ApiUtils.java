package com.github.madbrain.apiserver.services;

import com.github.madbrain.apiserver.api.ApiObject;

public class ApiUtils {
    public static <T extends ApiObject> String getName(T object) {
        if (object != null && object.getMetadata() != null) {
            return object.getMetadata().getName();
        }
        return null;
    }

    public static <T extends ApiObject> String getNamespace(T object) {
        if (object != null && object.getMetadata() != null) {
            return object.getMetadata().getNamespace();
        }
        return null;
    }
}
