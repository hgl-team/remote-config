package com.github.jlhuerfanor.config.remote;

import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.core.env.PropertySource;

public class SpringBootRemotePropertySource extends RemotePropertySource implements OriginLookup<String> {

    /**
     * Create a new {@code CompositePropertySource}.
     *
     * @param name the name of the property source
     */
    public SpringBootRemotePropertySource(String name) {
        super(name);
    }

    @Override
    public Origin getOrigin(String key) {
        for (PropertySource<?> propertySource : getPropertySources()) {
            if (propertySource instanceof OriginLookup) {
                OriginLookup lookup = (OriginLookup) propertySource;
                Origin origin = lookup.getOrigin(name);
                if (origin != null) {
                    return origin;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isImmutable() {
        return false;
    }
}
