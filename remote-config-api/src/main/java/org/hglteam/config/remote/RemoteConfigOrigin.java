package org.hglteam.config.remote;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class RemoteConfigOrigin {
    private String propertySourceName;
    private Object origin;
}
