package com.github.jlhuerfanor.config.remote;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class OriginTrackedPropertyValue<T> {
    private T origin;
    private Object value;
}
