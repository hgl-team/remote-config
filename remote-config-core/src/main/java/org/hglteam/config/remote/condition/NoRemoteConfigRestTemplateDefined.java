package org.hglteam.config.remote.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class NoRemoteConfigRestTemplateDefined implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !context.getBeanFactory().containsBean("remoteConfigRestTemplate");
    }
}
