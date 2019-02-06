package com.github.madbrain.apiserver.api;

import java.util.List;

public class ServiceAccountList extends ApiList<ServiceAccount> {
    public ServiceAccountList() {
        super("v1", "ServiceAccountList");
    }

    public ServiceAccountList(List<ServiceAccount> items) {
        this();
        getItems().addAll(items);
    }
}
