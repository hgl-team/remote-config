package org.hglteam.config.remote;

import com.google.gson.Gson;
import org.hglteam.config.remote.condition.NoRemoteConfigObjectFactoryDefined;
import org.hglteam.config.remote.condition.NoRemoteConfigRestTemplateDefined;
import org.hglteam.config.remote.condition.NoRemoteConfigServiceClientDefined;
import org.hglteam.config.remote.condition.NoRemoteConfigServicePropertiesDefined;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;

@Configuration
@Deprecated
public class RemoteConfiguration {
    private static final Logger log = LoggerFactory.getLogger(RemoteConfiguration.class);

    @Bean
    @Conditional(NoRemoteConfigServicePropertiesDefined.class)
    public static RemoteConfigRestServiceProperties remoteConfigRestServiceProperties(
            Environment environment) {
        return RemoteConfigRestServiceProperties.builder()
                .baseUris(RemoteConfiguration.splitLabelsOrEmpty(environment.getProperty("config.remote.url")))
                .name(environment.getProperty("config.remote.name"))
                .profile(environment.getProperty("config.remote.profile"))
                .labels(RemoteConfiguration.splitLabelsOrEmpty(environment.getProperty("config.remote.label")))
                .retry(Boolean.TRUE.equals(environment.getProperty("config.remote.retry", Boolean.class)))
                .readTimeout(environment.getProperty("config.remote.read-timeout", Integer.class, 1000))
                .connectTimeout(environment.getProperty("config.remote.connect-timeout", Integer.class, 1000))
                .build();
    }

    @Bean("remoteConfigRestTemplate")
    @Conditional(NoRemoteConfigRestTemplateDefined.class)
    public static RestTemplate restTemplate(RemoteConfigServiceProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        if (properties.getReadTimeout() < 0) {
            throw new IllegalStateException("Invalid Value for Read Timeout set.");
        }
        if (properties.getConnectTimeout() < 0) {
            throw new IllegalStateException("Invalid Value for Connect Timeout set.");
        }

        requestFactory.setReadTimeout(properties.getReadTimeout());
        requestFactory.setConnectTimeout(properties.getConnectTimeout());

        RestTemplate template = new RestTemplate(requestFactory);

        template.setMessageConverters(Arrays.asList(
                new ByteArrayHttpMessageConverter(),
                new StringHttpMessageConverter(),
                new FormHttpMessageConverter(),
                new GsonHttpMessageConverter(new Gson()),
                new ResourceHttpMessageConverter()
        ));

        return template;
    }

    @Bean
    @Conditional(NoRemoteConfigObjectFactoryDefined.class)
    public static RemoteConfigObjectFactory remoteConfigObjectFactory() {
        return new OriginTrackedPojoFactory();
    }

    @Bean
    @Conditional(NoRemoteConfigServiceClientDefined.class)
    public static RemoteConfigRestClient remoteConfigRestClient(
            RemoteConfigRestServiceProperties properties,
            @Qualifier("remoteConfigRestTemplate") RestTemplate restTemplate,
            RemoteConfigObjectFactory remoteConfigObjectFactory) {
        return new RemoteConfigRestClient(restTemplate, properties, remoteConfigObjectFactory);
    }

    @Bean
    @Order(0)
    public static PropertySourcesPlaceholderConfigurer remotePropertySource(
            // ApplicationContext context,
            ConfigurableApplicationContext configurableContext,
            RemoteConfigServiceClient client) {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        MutablePropertySources sources = new MutablePropertySources();
        PropertySource<?> propertySource;

        if(configurableContext.containsBean("remoteConfigRetryTemplate")) {
            RetryTemplate retryTemplate = configurableContext.getBean("remoteConfigRetryTemplate", RetryTemplate.class);

            propertySource = retryTemplate.execute(retryContext -> RemoteConfiguration.delegateFetch(
                    retryContext,
                    configurableContext.getEnvironment(),
                    client));
        } else {
            propertySource = client.fetch(configurableContext.getEnvironment());
        }

        sources.addLast(propertySource);
        configurableContext.getEnvironment()
                .getPropertySources()
                .addLast(propertySource);
        configurer.setPropertySources(sources);
        configurer.setIgnoreUnresolvablePlaceholders(true);
        configurer.setIgnoreResourceNotFound(true);

        return configurer;
    }

    private static String[] splitLabelsOrEmpty(String property) {
        return Optional.ofNullable(property)
                .map(s -> s.split(","))
                .orElse(new String[0]);
    }

    private static PropertySource<?> delegateFetch(RetryContext context, Environment environment, RemoteConfigServiceClient client) {
        if(context.getRetryCount() > 0) {
            log.info("Retrying configuration fetch. Attempt {}", context.getRetryCount());
        }
        return client.fetch(environment);
    }
}
