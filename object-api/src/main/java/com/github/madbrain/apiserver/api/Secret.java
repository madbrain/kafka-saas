package com.github.madbrain.apiserver.api;

public class Secret extends ApiObject {

    private String type;
    private Object data;

    public Secret() {
        super("v1", "Secret");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
