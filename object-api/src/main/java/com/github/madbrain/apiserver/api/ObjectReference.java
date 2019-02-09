package com.github.madbrain.apiserver.api;

public class ObjectReference {

    private String apiVersion;
    private String kind;
    private String name;
    private String namespace;

    public static ObjectReference from(ApiObject object) {
        ObjectReference ref = new ObjectReference();
        ref.apiVersion = object.getApiVersion();
        ref.kind = object.getKind();
        ref.name = object.getMetadata().getName();
        ref.namespace = object.getMetadata().getNamespace();
        return ref;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
