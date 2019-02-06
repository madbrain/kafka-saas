package com.github.madbrain.apiserver.api;

public class ApiObject extends ApiBase {

    private ObjectMeta metadata;

    private ApiObject() {
        super("", "");
    }

    public ApiObject(String apiVersion, String kind) {
        super(apiVersion, kind);
    }

    public ObjectMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
    }
}
