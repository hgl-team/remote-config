package org.hglteam.config.remote;

import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginLookup;

import java.util.Map;

public class SpringBootOriginTrackedPropertySource extends OriginTrackedPropertySource implements OriginLookup<String> {
    private final boolean inmutable;

    public SpringBootOriginTrackedPropertySource(String name, Map<String, Object> source, boolean inmutable) {
        super(name, source);
        this.inmutable = inmutable;
    }

    public SpringBootOriginTrackedPropertySource(String name, Map<String, Object> source) {
        this(name, source, false);
    }

    @Override
    public Origin getOrigin(String key) {
        return this.getOriginAs(key, Origin.class);
    }

    @Override
    public boolean isImmutable() {
        return this.inmutable;
    }
}
