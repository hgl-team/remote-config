package org.hglteam.config.remote;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class SerializableEnvironment {
    private String name;
    private String[] profiles = new String[0];
    private String label;
    private List<SerializablePropertySource> propertySources = new ArrayList<>();
    private String version;
    private String state;
}
