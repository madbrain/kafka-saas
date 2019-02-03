package com.github.madbrain.apiserver.services;

import com.github.madbrain.apiserver.services.ResourcePath;

import java.util.Collection;

public class ApiRequest {
    private String username;
    private Collection<String> groups;
    private String verb;
    private ResourcePath path;

    public ApiRequest(String username, Collection<String> groups, String verb, ResourcePath path) {
        this.username = username;
        this.groups = groups;
        this.verb = verb;
        this.path = path;
    }

    public ResourcePath getPath() {
        return path;
    }

    public String getUsername() {
        return username;
    }

    public Collection<String> getGroups() {
        return groups;
    }

    public String getVerb() {
        return verb;
    }
}
