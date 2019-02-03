package com.github.madbrain.apiserver.api;

import java.util.List;

public class ClusterRole extends ApiObject {

    private List<PolicyRule> rules;

    public ClusterRole() {
        super("v1", "ClusterRole");
    }

    public List<PolicyRule> getRules() {
        return rules;
    }

    public void setRules(List<PolicyRule> rules) {
        this.rules = rules;
    }
}
