package com.github.jlhuerfanor.config.remote.condition;

import com.github.jlhuerfanor.config.remote.RemoteConfigServiceProperties;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class NoRemoteConfigServicePropertiesDefined implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                context.getBeanFactory(),
                RemoteConfigServiceProperties.class)
                .length == 0;
    }
}
