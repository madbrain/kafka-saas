package com.github.madbrain.apiserver.api;

import java.util.List;

public class ServiceAccount extends ApiObject {

    private List<ObjectReference> secrets;

    public ServiceAccount() {
        super("v1", "ServiceAccount");
    }

    public List<ObjectReference> getSecrets() {
        return secrets;
    }

    public void setSecrets(List<ObjectReference> secrets) {
        this.secrets = secrets;
    }

    public static String makeUsername(String namespace, String name) {
        return "serviceaccount:" + namespace + ":" + name;
    }
}
