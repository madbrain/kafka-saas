package com.github.madbrain.apiserver.api;

import java.util.List;

public class ClusterRoleBindingList extends ApiList<ClusterRoleBinding> {
    public ClusterRoleBindingList(List<ClusterRoleBinding> items) {
        super("v1", "ClusterRoleBindingList");
        getItems().addAll(items);
    }
}
