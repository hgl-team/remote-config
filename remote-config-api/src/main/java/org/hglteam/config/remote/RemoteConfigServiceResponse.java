package org.hglteam.config.remote;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class RemoteConfigServiceResponse {
    private boolean success;
    private Exception exception;
    private Map<String, String> properties;
}
