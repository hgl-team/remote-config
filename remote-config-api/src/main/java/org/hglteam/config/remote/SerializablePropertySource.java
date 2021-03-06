package org.hglteam.config.remote;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class SerializablePropertySource {
    private String name;
    private Map<?,?> source;

    @Override
    public String toString() {
        return "SerializablePropertySource {" +
                "name='" + name + '\'' +
                '}';
    }
}
