package com.github.madbrain.apiserver.api;

public class Namespace extends ApiObject {
    public Namespace(String name) {
        super("v1", "Namespace");
        setMetadata(new ObjectMeta());
        getMetadata().setName(name);
    }
}
