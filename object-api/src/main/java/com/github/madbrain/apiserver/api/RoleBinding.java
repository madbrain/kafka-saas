package com.github.madbrain.apiserver.api;

import java.util.ArrayList;
import java.util.List;

public class RoleBinding extends ApiObject {

    private RoleRef roleRef;
    private List<Subject> subjects = new ArrayList<>();

    public RoleBinding() {
        super("v1", "RoleBinding");
    }

    public RoleRef getRoleRef() {
        return roleRef;
    }

    public void setRoleRef(RoleRef roleRef) {
        this.roleRef = roleRef;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

}
