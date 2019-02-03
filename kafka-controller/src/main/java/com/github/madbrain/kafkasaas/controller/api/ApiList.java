package com.github.madbrain.kafkasaas.controller.api;

import java.util.ArrayList;
import java.util.List;

public class ApiList<T extends ApiObject> extends ApiBase {

    private List<T> items = new ArrayList<>();

    public ApiList(String apiVersion, String kind) {
        super(apiVersion, kind);
    }

    public List<T> getItems() {
        return items;
    }
}
