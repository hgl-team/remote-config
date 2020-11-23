package com.github.jlhuerfanor.config.remote;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.origin.Origin;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class SpringBootRemoteConfigOrigin extends RemoteConfigOrigin implements Origin {
}
