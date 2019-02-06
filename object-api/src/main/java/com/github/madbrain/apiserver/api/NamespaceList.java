package com.github.madbrain.apiserver.api;

import java.util.List;

public class NamespaceList extends ApiList<Namespace> {
    public NamespaceList(List<Namespace> namespaces) {
        super("v1", "NamespaceList");
        getItems().addAll(namespaces);
    }
}
