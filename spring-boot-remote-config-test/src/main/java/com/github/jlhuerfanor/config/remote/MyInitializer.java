package com.github.jlhuerfanor.config.remote;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

public class MyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>{
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ClassPathResource resource = new ClassPathResource("bootstrap.yml");
        EncodedResource r = new EncodedResource(resource);

        try {
            applicationContext.getEnvironment().getPropertySources()
                    .addFirst(new YAMLPropertySourceFactory().createPropertySource("bootstrap", r));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
