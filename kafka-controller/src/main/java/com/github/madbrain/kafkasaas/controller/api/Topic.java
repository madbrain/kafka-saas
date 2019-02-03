package com.github.madbrain.kafkasaas.controller.api;

public class Topic extends ApiObject {

    private Spec spec;

    public Topic() {
        super("v1", "Topic");
    }

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec = spec;
    }


    public static class Spec {
        private String topic;
        private String size;
        private String retention;
        private int partitions;
        private short replicationFactor;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getRetention() {
            return retention;
        }

        public void setRetention(String retention) {
            this.retention = retention;
        }

        public int getPartitions() {
            return partitions;
        }

        public void setPartitions(int partitions) {
            this.partitions = partitions;
        }

        public short getReplicationFactor() {
            return replicationFactor;
        }

        public void setReplicationFactor(short replicationFactor) {
            this.replicationFactor = replicationFactor;
        }
    }

}
