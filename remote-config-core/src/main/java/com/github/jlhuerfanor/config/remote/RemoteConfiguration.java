package com.github.jlhuerfanor.config.remote;

import com.github.jlhuerfanor.config.remote.condition.NoRemoteConfigObjectFactoryDefined;
import com.github.jlhuerfanor.config.remote.condition.NoRemoteConfigRestTemplateDefined;
import com.github.jlhuerfanor.config.remote.condition.NoRemoteConfigServiceClientDefined;
import com.github.jlhuerfanor.config.remote.condition.NoRemoteConfigServicePropertiesDefined;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
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
            ApplicationContext context,
            RemoteConfigServiceClient client) {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        MutablePropertySources sources = new MutablePropertySources();
        PropertySource<?> propertySource;

        if(context.containsBean("remoteConfigRetryTemplate")) {
            RetryTemplate retryTemplate = context.getBean("remoteConfigRetryTemplate", RetryTemplate.class);

            propertySource = retryTemplate.execute(retryContext -> RemoteConfiguration.delegateFetch(
                    retryContext,
                    context.getEnvironment(),
                    client));
        } else {
            propertySource = client.fetch(context.getEnvironment());
        }

        sources.addLast(propertySource);
        configurer.setPropertySources(sources);
        configurer.setIgnoreUnresolvablePlaceholders(true);
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
