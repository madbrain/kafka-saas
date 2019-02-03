package com.github.madbrain.apiserver.test;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.madbrain.apiserver.api.*;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class TestSchema {

    private ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Test
    public void testRole() throws IOException, ProcessingException {
        Role role = new Role();
        role.setMetadata(new ObjectMeta());
        role.getMetadata().setName("NAME");
        PolicyRule rule = new PolicyRule();
        rule.setResources(Collections.emptyList());
        rule.setVerbs(Arrays.asList("GET"));
        role.getRules().add(rule);

        Assertions.assertThat(test(role, "/schemas/roles.json")).isTrue();
    }

    @Test
    public void testRoleBinding() throws IOException, ProcessingException {
        RoleBinding roleBinding = new RoleBinding();
        roleBinding.setMetadata(new ObjectMeta());
        roleBinding.getMetadata().setName("NAME");
        RoleRef roleRef = new RoleRef();
        roleBinding.setRoleRef(roleRef);
        Subject subject = new Subject();
        roleBinding.getSubjects().add(subject);
        PolicyRule rule = new PolicyRule();
        rule.setResources(Collections.emptyList());

        Assertions.assertThat(test(roleBinding, "/schemas/rolebindings.json")).isTrue();
    }

    @Test
    public void testClusterRole() throws IOException, ProcessingException {
        ClusterRole clusterRole = new ClusterRole();
        clusterRole.setMetadata(new ObjectMeta());
        clusterRole.getMetadata().setName("NAME");
        PolicyRule rule = new PolicyRule();
        rule.setResources(Collections.emptyList());
        rule.setVerbs(Arrays.asList("GET"));
        clusterRole.setRules(Arrays.asList(rule));

        Assertions.assertThat(test(clusterRole, "/schemas/clusterroles.json")).isTrue();
    }

    @Test
    public void testClusterRoleBinding() throws IOException, ProcessingException {
        ClusterRoleBinding clusterRoleBinding = new ClusterRoleBinding();
        clusterRoleBinding.setMetadata(new ObjectMeta());
        clusterRoleBinding.getMetadata().setName("NAME");
        RoleRef roleRef = new RoleRef();
        Subject subject = new Subject();
        clusterRoleBinding.setRoleRef(roleRef);
        clusterRoleBinding.setSubjects(Arrays.asList(subject));

        Assertions.assertThat(test(clusterRoleBinding, "/schemas/clusterrolebindings.json")).isTrue();
    }

    private boolean test(Object object, String path) throws ProcessingException, IOException {

        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonNode schemaNode = mapper.readTree(getClass().getResourceAsStream(path));
        final JsonSchema schema = factory.getJsonSchema(schemaNode);

        ProcessingReport report = schema.validate(mapper.valueToTree(object));
        report.forEach(x -> {
            System.out.println(x);
        });
        return report.isSuccess();
    }
}
