package com.github.madbrain.apiserver.services;

public interface Authorizer {

    boolean authorize(ApiRequest request);
}
