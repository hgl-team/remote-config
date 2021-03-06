package org.hglteam.config.remote;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableAutoConfiguration
public class SpringBootRemoteConfiguration {
    @Bean
    @Primary
    public static SpringBootOriginTrackedObjectFactory springBootOriginTrackedObjectFactory() {
        return new SpringBootOriginTrackedObjectFactory();
    }

    @Configuration
    @Import({ RemoteConfiguration.class })
    public static class SpringRemoteConfiguration {
    }
}
