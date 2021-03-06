package org.hglteam.config.remote.condition;

import org.hglteam.config.remote.RemoteConfigServiceClient;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class NoRemoteConfigServiceClientDefined implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context.getBeanFactory(), RemoteConfigServiceClient.class)
                .length == 0;
    }
}
