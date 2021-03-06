package org.hglteam.config.remote;

import org.springframework.core.env.MapPropertySource;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class OriginTrackedPropertySource extends MapPropertySource {

    public OriginTrackedPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    protected <T> Optional<T> getProperty(String name, Function<Object, T> parser) {
        return Optional.ofNullable(super.getProperty(name))
                .map(parser);
    }

    @Override
    public Object getProperty(String name) {
        Object value = super.getProperty(name);

        return Optional.ofNullable(value)
                .filter(OriginTrackedPropertyValue.class::isInstance)
                .map(OriginTrackedPropertyValue.class::cast)
                .map(OriginTrackedPropertyValue::getValue)
                .orElse(value);
    }

    public Optional<?> getOriginOf(String name) {
        Object value = super.getProperty(name);

        return Optional.ofNullable(value)
                .filter(OriginTrackedPropertyValue.class::isInstance)
                .map(OriginTrackedPropertyValue.class::cast)
                .map(OriginTrackedPropertyValue::getOrigin);
    }

    public <T> T getOriginAs(String name, Class<T> originClass) {
        return OriginTrackedPropertySource.this.getOriginOf(name)
                .filter(originClass::isInstance)
                .map(originClass::cast)
                .orElse(null);
    }
}
