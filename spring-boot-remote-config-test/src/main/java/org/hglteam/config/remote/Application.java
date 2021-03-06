package org.hglteam.config.remote;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(Application.class)
                .initializers(new MyInitializer())
                .build()
                .run(args);
    }

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        printActiveProperties((ConfigurableEnvironment) event.getApplicationContext().getEnvironment());
    }

    private void printActiveProperties(ConfigurableEnvironment env) {

        System.out.println("************************* ACTIVE APP PROPERTIES ******************************");

        env.getPropertySources().forEach(this::printProperties);

        System.out.println("******************************************************************************");
    }

    private void printProperties(PropertySource<?> propertySource) {
        if(propertySource instanceof CompositePropertySource) {
            CompositePropertySource composite = (CompositePropertySource)propertySource;

            composite.getPropertySources().forEach(this::printProperties);
        } else if(propertySource instanceof MapPropertySource) {
            MapPropertySource source = (MapPropertySource)propertySource;

            source.getSource().entrySet().forEach(entry -> {
                System.out.printf("%s = %s", entry.getKey(), entry.getValue());
                System.out.println();
            });
        } else {
            System.out.println(propertySource.getClass());
        }
    }
}
