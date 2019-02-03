package com.github.madbrain.apiserver.api;

import java.util.Collection;
import java.util.List;

public class RoleList extends ApiList<Role> {

    public RoleList() {
        super("v1", "RoleList");
    }

    public RoleList(List<Role> roles) {
        this();
        getItems().addAll(roles);
    }

    public static RoleList of(Collection<Role> roles) {
        RoleList result = new RoleList();
        result.getItems().addAll(roles);
        return result;
    }
}
