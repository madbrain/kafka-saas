package com.github.madbrain.apiserver.api;

import java.util.List;

public class ClusterRoleList extends ApiList<ClusterRole> {
    public ClusterRoleList(List<ClusterRole> items) {
        super("v1", "ClusterRoleList");
        getItems().addAll(items);
    }
}
