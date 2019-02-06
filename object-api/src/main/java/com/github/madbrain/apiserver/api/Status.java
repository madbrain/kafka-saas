package com.github.madbrain.apiserver.api;

public class Status extends ApiBase {
    private String message;

    public Status() {
        super("v1", "Status");
    }

    public String getMessage() {
        return message;
    }

    public static Status of(String message) {
        Status status = new Status();
        status.message = message;
        return status;
    }
}
