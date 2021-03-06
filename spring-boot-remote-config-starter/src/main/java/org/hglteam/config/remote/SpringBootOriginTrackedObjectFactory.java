package org.hglteam.config.remote;

import java.util.Map;

public class SpringBootOriginTrackedObjectFactory implements RemoteConfigObjectFactory {
    @Override
    public OriginTrackedPropertySource createPropertySource(String propertySourceName, Map<String, Object> properties) {
        return new SpringBootOriginTrackedPropertySource(propertySourceName, properties);
    }

    @Override
    public RemotePropertySource createRemotePropertySource(String propertySourceName) {
        return new SpringBootRemotePropertySource(propertySourceName);
    }

    @Override
    public OriginTrackedPropertyValue<?> createPropertyValue(String propertySourceName, Object origin, Object value) {
        return SpringBootOriginTrackedPropertyValue.<SpringBootRemoteConfigOrigin>builder()
                .origin(SpringBootRemoteConfigOrigin.builder()
                        .propertySourceName(propertySourceName)
                        .origin(origin)
                        .build())
                .value(value)
                .build();
    }
}
