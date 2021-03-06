package org.hglteam.config.remote;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginProvider;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true, builderMethodName = "instanceBuilder")
public class SpringBootOriginTrackedPropertyValue<T extends Origin> extends OriginTrackedPropertyValue<T>
    implements OriginProvider {
}
