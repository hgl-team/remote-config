package com.github.jlhuerfanor.config.remote;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class RemoteConfigRestServiceProperties extends RemoteConfigServiceProperties {
    private String[] baseUris;
}
