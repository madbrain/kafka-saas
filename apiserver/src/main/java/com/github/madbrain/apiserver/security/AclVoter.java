package com.github.madbrain.apiserver.security;

import com.github.madbrain.apiserver.services.ApiRequest;
import com.github.madbrain.apiserver.services.ResourcePath;
import com.github.madbrain.apiserver.services.ResourcePathBuilder;
import com.github.madbrain.apiserver.services.Authorizer;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AclVoter implements AccessDecisionVoter<FilterInvocation> {

    private Authorizer authorizer;

    public AclVoter(Authorizer authorizer) {
        this.authorizer = authorizer;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

    @Override
    public int vote(Authentication authentication, FilterInvocation fi, Collection<ConfigAttribute> attributes) {
        if (fi.getRequestUrl().startsWith("/api/v1/")) {
            ResourcePath path = new ResourcePathBuilder().build(fi.getRequestUrl().substring("/api/v1/".length()));
            ApiRequest request = new ApiRequest(
                    authentication.getName(),
                    getGroups(authentication),
                    fi.getHttpRequest().getMethod(),
                    path);
            return authorizer.authorize(request) ? ACCESS_GRANTED : ACCESS_DENIED;
        }
        return ACCESS_ABSTAIN;
    }

    private List<String> getGroups(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}
