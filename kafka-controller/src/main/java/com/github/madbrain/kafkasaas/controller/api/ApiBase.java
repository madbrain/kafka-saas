package com.github.madbrain.kafkasaas.controller.api;

public abstract class ApiBase {

    private String apiVersion;
    private String kind;

    public ApiBase(String apiVersion, String kind) {
        this.apiVersion = apiVersion;
        this.kind = kind;
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
}
