
# Kafka Controller

Synchronize state from ApiServer to the actual Kafka server.

For example, the declaration of this topic object :

```json
{
  "kind": "Topic",
  "apiVersion": "v1",
  "metadata": {
    "namespace": "myproject",
    "name": "article"
  },
  "spec": {
    "topic": "article-topic",
    "partitions": 3,
    "replicationFactor": 1,
    "size": "100M",
    "retention": "1"
  }
}
```

will create the topic with the specified information.