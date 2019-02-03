
create table objects (
  uid varchar(36) primary key,
  namespace text,
  type text,
  name text,
  content text
);

insert into objects(uid, namespace, type, name, content) values ('ba7a0f25-9cac-4416-9021-f8d8dacf1f5b', null, 'clusterroles', 'admin', '{
  "kind": "ClusterRole",
  "apiVersion": "v1",
  "metadata": {
    "name": "admin"
  },
  "rules": [
    {
      "apiGroups": [""],
      "resources": ["ResourceAll"],
      "verbs": ["VerbAll"]
    }
  ]
}');

insert into objects(uid, namespace, type, name, content) values ('700f9cf0-0a04-4d97-a19d-7cddbea6276c', null, 'clusterrolebindings', 'admin', '{
  "kind": "ClusterRoleBinding",
  "apiVersion": "v1",
  "metadata": {
    "name": "admin"
  },
  "subjects": [
    {
      "kind": "Group",
      "name": "ADMIN",
      "apiGroup": "rbac.authorization.k8s.io"
    }
  ],
  "roleRef": {
    "kind": "ClusterRole",
    "name": "admin",
    "apiGroup": "rbac.authorization.k8s.io"
  }
}');