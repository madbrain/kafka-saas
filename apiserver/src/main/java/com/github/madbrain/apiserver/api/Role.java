package com.github.madbrain.apiserver.api;

import java.util.ArrayList;
import java.util.List;

public class Role extends ApiObject {
    private List<PolicyRule> rules = new ArrayList<>();

    public Role() {
        super("v1", "Role");
    }

    public List<PolicyRule> getRules() {
        return rules;
    }

}
