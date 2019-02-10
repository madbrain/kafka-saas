package com.github.madbrain.apiserver.services.impl;

import com.github.madbrain.apiserver.api.*;
import com.github.madbrain.apiserver.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class AuthorizerImpl implements Authorizer {

    private ObjectStore objectStore;
    private ResourceDescriptorRegistry registry;

    private static class Context {

        private ClusterRoleBinding crb;
        private RoleBinding rb;
        private Subject subject;
        private PolicyRule rule;

        public static Context create(RoleBinding rb, Subject subject) {
            Context context = new Context();
            context.rb = rb;
            context.subject = subject;
            return context;
        }

        public static Context create(ClusterRoleBinding crb, Subject subject) {
            Context context = new Context();
            context.crb = crb;
            context.subject = subject;
            return context;
        }

        public Context rule(PolicyRule rule) {
            Context context = new Context();
            context.rb = rb;
            context.crb = crb;
            context.subject = subject;
            context.rule = rule;
            return context;
        }
    }

    @Autowired
    public AuthorizerImpl(ObjectStore objectStore, ResourceDescriptorRegistry registry) {
        this.objectStore = objectStore;
        this.registry = registry;
    }

    @Override
    public boolean authorize(ApiRequest request) {
        if (!StringUtils.isEmpty(request.getPath().getNamespace())) {
            if (objectStore.getAll(request.getPath().getNamespace(), registry.get("rolebindings"), RoleBindingList.class).getItems().stream()
                    .flatMap(rb -> appliesTo(rb.getSubjects(), request, request.getPath().getNamespace())
                            .map(subject -> Stream.of(Context.create(rb, subject)))
                            .orElse(Stream.empty()))
                    .flatMap(context -> getRulesFrom(request.getPath().getNamespace(), context.rb.getRoleRef()).map(context::rule))
                    .anyMatch(context -> ruleAllows(context, request))) {
                return true;
            }
        }
        return objectStore.getAll(null, registry.get("clusterrolebindings"), ClusterRoleBindingList.class).getItems().stream()
                .flatMap(crb -> appliesTo(crb.getSubjects(), request, "")
                        .map(subject -> Stream.of(Context.create(crb, subject)))
                        .orElse(Stream.empty()))
                .flatMap(context -> getRulesFrom(null, context.crb.getRoleRef()).map(context::rule))
                .anyMatch(context -> ruleAllows(context, request));
    }

    private boolean ruleAllows(Context context, ApiRequest request) {
        PolicyRule rule = context.rule;
        if (rule.getVerbs().stream().noneMatch(v -> matchVerb(v, request.getVerb()))) {
            return false;
        }
        if (rule.getResources().stream().noneMatch(r -> matchResource(r, request.getPath().getResourceType()))) {
            return false;
        }
        if (rule.getResourceNames() != null
                && !rule.getResourceNames().isEmpty()
                && rule.getResourceNames().stream().noneMatch(rn -> matchResourceNames(rn, request.getPath().getResourceName()))) {
            return false;
        }
        return true;
    }

    private boolean matchVerb(String expectedVerb, String verb) {
        return expectedVerb.equals("VerbAll")
                || expectedVerb.equalsIgnoreCase(verb);
    }

    private boolean matchResource(String expectedResource, String resource) {
        return expectedResource.equals("ResourceAll")
                || expectedResource.equals(resource);
    }

    private boolean matchResourceNames(String expectedResourceName, String resourceName) {
        return expectedResourceName.equals(resourceName);
    }

    private Stream<PolicyRule> getRulesFrom(String namespace, RoleRef roleRef) {
        if (roleRef.getKind().equals("Role")) {
            return Optional.ofNullable(objectStore.get(namespace, roleRef.getName(), registry.get("roles"), Role.class))
                    .map(Role::getRules)
                    .orElse(Collections.emptyList()).stream();
        }
        if (roleRef.getKind().equals("ClusterRole")) {
            return Optional.ofNullable(objectStore.get(null, roleRef.getName(), registry.get("clusterroles"), ClusterRole.class))
                    .map(ClusterRole::getRules)
                    .orElse(Collections.emptyList()).stream();
        }
        return Stream.empty();
    }

    private static Optional<Subject> appliesTo(List<Subject> subjects, ApiRequest request, String namespace) {
        return subjects.stream()
                .filter(subject -> appliesTo(subject, request, namespace))
                .findFirst();
    }

    private static boolean appliesTo(Subject subject, ApiRequest request, String namespace) {
        if (subject.getKind().equals("User")) {
            return subject.getName().equals(request.getUsername());
        } else if (subject.getKind().equals("Group")) {
            return request.getGroups().contains(subject.getName());
        } else if (subject.getKind().equals("ServiceAccount")) {
            String saNamespace = StringUtils.isEmpty(subject.getNamespace()) ? namespace : subject.getNamespace();
            if (saNamespace.isEmpty()) {
                return false;
            }
            return ServiceAccount.makeUsername(saNamespace, subject.getName()).equals(request.getUsername());
        } else {
            return false;
        }
    }


}
