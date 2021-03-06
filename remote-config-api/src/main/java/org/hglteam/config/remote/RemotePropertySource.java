package org.hglteam.config.remote;

import org.springframework.core.env.CompositePropertySource;

import java.util.Objects;
import java.util.Optional;

public class RemotePropertySource extends CompositePropertySource {
    /**
     * Create a new {@code CompositePropertySource}.
     *
     * @param name the name of the property source
     */
    public RemotePropertySource(String name) {
        super(name);
    }

    public Optional<?> getOriginOf(String propertyName) {
        return this.getPropertySources().stream()
                .filter(OriginTrackedPropertySource.class::isInstance)
                .map(OriginTrackedPropertySource.class::cast)
                .map(propertySource -> propertySource.getOriginOf(propertyName))
                .filter(Objects::nonNull)
                .findAny();
    }

    public <T> T getOriginAs(String propertyName, Class<T> originClass) {
        return getOriginOf(propertyName)
                .filter(originClass::isInstance)
                .map(originClass::cast)
                .orElse(null);
    }
}
