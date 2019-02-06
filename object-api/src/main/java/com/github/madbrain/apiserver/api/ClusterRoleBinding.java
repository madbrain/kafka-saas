package com.github.madbrain.apiserver.api;

import java.util.ArrayList;
import java.util.List;

public class ClusterRoleBinding extends ApiObject {

    private RoleRef roleRef;
    private List<Subject> subjects = new ArrayList<>();

    public ClusterRoleBinding() {
        super("v1", "ClusterRoleBinding");
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

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }
}
