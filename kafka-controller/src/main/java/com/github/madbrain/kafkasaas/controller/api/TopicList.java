package com.github.madbrain.kafkasaas.controller.api;

import com.github.madbrain.apiserver.api.ApiList;

public class TopicList extends ApiList<Topic> {
    public TopicList() {
        super("v1", "TopicList");
    }
}
