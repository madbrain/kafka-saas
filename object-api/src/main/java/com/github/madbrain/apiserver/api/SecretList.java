package com.github.madbrain.apiserver.api;

import java.util.List;

public class SecretList extends ApiList<Secret> {
    public SecretList() {
        super("v1", "SecretList");
    }

    public SecretList(List<Secret> items) {
        this();
        getItems().addAll(items);
    }
}
