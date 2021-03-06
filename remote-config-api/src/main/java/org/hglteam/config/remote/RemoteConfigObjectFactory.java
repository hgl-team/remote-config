package org.hglteam.config.remote;

import java.util.Map;

public interface RemoteConfigObjectFactory {
    OriginTrackedPropertyValue<?> createPropertyValue(String propertySourceName, Object origin, Object value);
    OriginTrackedPropertySource createPropertySource(String propertySourceName, Map<String, Object> properties);
    RemotePropertySource createRemotePropertySource(String propertySourceName);
}
