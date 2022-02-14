package org.hglteam.config.remote.condition;

import org.hglteam.config.remote.RemoteConfigObjectFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Deprecated
public class NoRemoteConfigObjectFactoryDefined implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context.getBeanFactory(), RemoteConfigObjectFactory.class)
                .length == 0;
    }
}
