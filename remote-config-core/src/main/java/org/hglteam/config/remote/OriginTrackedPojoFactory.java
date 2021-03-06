package org.hglteam.config.remote;

import java.util.Map;

public class OriginTrackedPojoFactory implements RemoteConfigObjectFactory {

    public OriginTrackedPropertySource createPropertySource(String propertySourceName, Map<String, Object> properties) {
        return new OriginTrackedPropertySource(propertySourceName, properties);
    }

    @Override
    public RemotePropertySource createRemotePropertySource(String propertySourceName) {
        return new RemotePropertySource(propertySourceName);
    }

    public OriginTrackedPropertyValue<?> createPropertyValue(String propertySourceName, Object origin, Object value) {
        return OriginTrackedPropertyValue.<RemoteConfigOrigin>builder()
                .origin(RemoteConfigOrigin.builder()
                        .propertySourceName(propertySourceName)
                        .origin(origin)
                        .build())
                .value(value)
                .build();
    }
}
