package com.github.madbrain.apiserver.api;

import java.util.List;

public class PolicyRule {
    private List<String> apiGroups;
    private List<String> resourceNames;
    private List<String> resources;
    private List<String> verbs;

    public List<String> getApiGroups() {
        return apiGroups;
    }

    public void setApiGroups(List<String> apiGroups) {
        this.apiGroups = apiGroups;
    }

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    public List<String> getVerbs() {
        return verbs;
    }

    public void setVerbs(List<String> verbs) {
        this.verbs = verbs;
    }

    public List<String> getResourceNames() {
        return resourceNames;
    }

    public void setResourceNames(List<String> resourceNames) {
        this.resourceNames = resourceNames;
    }
}
