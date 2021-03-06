package org.hglteam.config.remote;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class RemoteConfigServiceProperties {
    private String name;
    private String profile;
    private String[] labels;
    private int readTimeout;
    private int connectTimeout;

    private boolean retry;
}
