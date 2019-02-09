package com.github.madbrain.apiserver.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.madbrain.apiserver.services.ApiUtils;
import com.github.madbrain.apiserver.services.ObjectStore;
import com.github.madbrain.apiserver.services.ResourceDescriptor;
import com.github.madbrain.apiserver.api.ApiList;
import com.github.madbrain.apiserver.api.ApiObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class DatabaseObjectStore implements ObjectStore {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private class ApiObjectMapper implements RowMapper<ApiObject> {

        private Class<? extends ApiObject> resourceClass;

        public ApiObjectMapper(Class<? extends ApiObject> resourceClass) {
            this.resourceClass = resourceClass;
        }

        @Override
        public ApiObject mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                return objectMapper.readValue(rs.getString("content"), resourceClass);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class ApiObjectJSonMapper implements RowMapper<JsonNode> {

        @Override
        public JsonNode mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                return objectMapper.readTree(rs.getString("content"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public <T extends ApiObject> T get(String namespace, String name, ResourceDescriptor descriptor, Class<T> cls) {
        if (namespace == null) {
            List<? extends ApiObject> result = jdbcTemplate.query("SELECT * FROM objects WHERE namespace IS NULL AND type = ? AND name = ?",
                    new Object[]{descriptor.getResourceType(), name}, new ApiObjectMapper(descriptor.getResourceClass()));
            return result.size() > 0 ? cls.cast(result.get(0)) : null;
        } else {
            List<? extends ApiObject> result = jdbcTemplate.query("SELECT * FROM objects WHERE namespace = ? AND type = ? AND name = ?",
                    new Object[]{namespace, descriptor.getResourceType(), name}, new ApiObjectMapper(descriptor.getResourceClass()));
            return result.size() > 0 ? cls.cast(result.get(0)) : null;
        }
    }

    @Override
    public JsonNode getRaw(String namespace, String name, ResourceDescriptor descriptor) {
        if (namespace == null) {
            List<JsonNode> result = jdbcTemplate.query("SELECT * FROM objects WHERE type = ? AND name = ?",
                    new Object[]{descriptor.getResourceType(), name}, new ApiObjectJSonMapper());
            return result.size() > 0 ? result.get(0) : null;
        } else {
            List<JsonNode> result = jdbcTemplate.query("SELECT * FROM objects WHERE namespace = ? AND type = ? AND name = ?",
                    new Object[]{namespace, descriptor.getResourceType(), name}, new ApiObjectJSonMapper());
            return result.size() > 0 ? result.get(0) : null;
        }
    }

    @Override
    public void put(ApiObject object, JsonNode node, ResourceDescriptor descriptor) {
        String namespace = ApiUtils.getNamespace(object);
        List<String> uids = jdbcTemplate.queryForList("SELECT uid from objects WHERE namespace = ? AND type = ? AND name = ?",
                new Object[]{namespace, descriptor.getResourceType(), object.getMetadata().getName()}, String.class);
        String uid;
        boolean exists = false;
        if (uids.isEmpty()) {
            uid = UUID.randomUUID().toString();
        } else {
            exists = true;
            uid = uids.get(0);
        }
        object.getMetadata().setUid(uid);
        try {
            if (exists) {
                jdbcTemplate.update("UPDATE objects SET namespace = ?, type = ?, name = ?, content = ? WHERE uid = ?",
                        namespace, descriptor.getResourceType(),
                        object.getMetadata().getName(),
                        objectMapper.writeValueAsString(node),
                        object.getMetadata().getUid());
            } else {
                jdbcTemplate.update("INSERT INTO objects(uid, namespace, type, name, content) VALUES(?,?,?,?,?)",
                        object.getMetadata().getUid(), namespace, descriptor.getResourceType(),
                        object.getMetadata().getName(), objectMapper.writeValueAsString(node));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends ApiList> T getAll(String namespace, ResourceDescriptor descriptor, Class<T> cls) {
        List<ApiObject> objects;
        if (StringUtils.isEmpty(namespace)) {
            objects = jdbcTemplate.query("SELECT * FROM objects WHERE type = ?",
                    new Object[]{descriptor.getResourceType()}, new ApiObjectMapper(descriptor.getResourceClass()));
        } else {
            objects = jdbcTemplate.query("SELECT * FROM objects WHERE namespace = ? AND type = ?",
                    new Object[]{namespace, descriptor.getResourceType()}, new ApiObjectMapper(descriptor.getResourceClass()));
        }
        return cls.cast(descriptor.buildList(objects));
    }

    @Override
    public JsonNode getRawAll(String namespace, ResourceDescriptor descriptor) {
        List<JsonNode> objects;
        if (StringUtils.isEmpty(namespace)) {
            objects = jdbcTemplate.query("SELECT * FROM objects WHERE type = ?",
                    new Object[]{descriptor.getResourceType()}, new ApiObjectJSonMapper());
        } else {
            objects = jdbcTemplate.query("SELECT * FROM objects WHERE namespace = ? AND type = ?",
                    new Object[]{namespace, descriptor.getResourceType()}, new ApiObjectJSonMapper());
        }
        return buildRawList(descriptor.getListKind(), objects);
    }

    private JsonNode buildRawList(String listKind, List<JsonNode> objects) {
        ObjectNode node = objectMapper.createObjectNode();
        ArrayNode items = objectMapper.createArrayNode();
        items.addAll(objects);
        String[] nameAndVersion = listKind.split(":");
        node.put("kind", nameAndVersion[0]);
        node.put("apiVersion", nameAndVersion[1]);
        node.put("items", items);
        return node;
    }

    @Override
    public void delete(String namespace, String name, ResourceDescriptor descriptor) {
        if (StringUtils.isEmpty(namespace)) {
            jdbcTemplate.update("DELETE FROM objects WHERE type = ? AND name = ?",
                    descriptor.getResourceType(), name);
        } else {
            jdbcTemplate.update("DELETE FROM objects WHERE namespace = ? AND type = ? AND name = ?",
                    namespace, descriptor.getResourceType(), name);
        }
    }
}
