package com.github.madbrain.apiserver.api;

import java.util.List;

public class RoleBindingList extends ApiList<RoleBinding> {

    public RoleBindingList(List<RoleBinding> roleBindings) {
        super("v1", "RoleBindingList");
        getItems().addAll(roleBindings);
    }
}
