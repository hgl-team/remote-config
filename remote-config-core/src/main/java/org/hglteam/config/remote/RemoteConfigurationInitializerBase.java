package org.hglteam.config.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import java.util.Optional;

public abstract class RemoteConfigurationInitializerBase<
        TClient extends RemoteConfigServiceClient,
        TProperties extends RemoteConfigServiceProperties,
        TPropertyBuilder extends RemoteConfigServiceProperties.RemoteConfigServicePropertiesBuilder<? extends TProperties, ?>>
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger log = LoggerFactory.getLogger(RemoteConfiguration.class);
    private static final String[] EMPTY_ARRAY = new String[0];
    private static final String CONFIG_REMOTE_RETRY_TEMPLATE_INITIAL_INTERVAL = "config.remote.retry-template.initial-interval";
    private static final String CONFIG_REMOTE_RETRY_TEMPLATE_MAX_INTERVAL = "config.remote.retry-template.max-interval";
    private static final String CONFIG_REMOTE_RETRY_TEMPLATE_MULTIPLIER = "config.remote.retry-template.multiplier";
    private static final String CONFIG_REMOTE_RETRY_TEMPLATE_MAX_ATTEMPS = "config.remote.retry-template.max-attemps";
    private static final String CONFIG_REMOTE_NAME = "config.remote.name";
    private static final String CONFIG_REMOTE_PROFILE = "config.remote.profile";
    private static final String CONFIG_REMOTE_LABEL = "config.remote.label";
    private static final String CONFIG_REMOTE_RETRY = "config.remote.retry";
    private static final String CONFIG_REMOTE_READ_TIMEOUT = "config.remote.read-timeout";
    private static final String CONFIG_REMOTE_CONNECT_TIMEOUT = "config.remote.connect-timeout";

    private static final String URL_SEPARATOR = " ";

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        RemoteConfigObjectFactory objectFactory = createRemoteConfigObjectFactory();
        RetryTemplate retryTemplate = createRetryTemplate(context.getEnvironment());
        TProperties serviceProperties = getRemoteConfigServiceProperties(context.getEnvironment());
        TClient remoteConfigServiceClient = createRemoteConfigServiceClient(serviceProperties, objectFactory);

        PropertySource<?> propertySource = retryTemplate.execute(retryContext -> delegateFetch(
                retryContext,
                context.getEnvironment(),
                remoteConfigServiceClient));

        context.getEnvironment().getPropertySources().addLast(propertySource);
    }

    protected RetryTemplate createRetryTemplate(Environment e) {
        int initialInterval = e.getProperty(CONFIG_REMOTE_RETRY_TEMPLATE_INITIAL_INTERVAL, Integer.class, 1000);
        int maxInterval = e.getProperty(CONFIG_REMOTE_RETRY_TEMPLATE_MAX_INTERVAL, Integer.class, 10000);
        double multiplier = e.getProperty(CONFIG_REMOTE_RETRY_TEMPLATE_MULTIPLIER, Double.class, 1.6);
        int attempts = e.getProperty(CONFIG_REMOTE_RETRY_TEMPLATE_MAX_ATTEMPS, Integer.class, 5);

        return RetryTemplate.builder()
                .exponentialBackoff(initialInterval, multiplier, maxInterval)
                .maxAttempts(attempts)
                .build();
    }

    protected abstract TPropertyBuilder getRemoteConfigClientPropertyBuilder(Environment environment);

    protected abstract TClient createRemoteConfigServiceClient(TProperties properties, RemoteConfigObjectFactory objectFactory);

    private TProperties getRemoteConfigServiceProperties(Environment environment) {
        return getRemoteConfigClientPropertyBuilder(environment)
                .name(environment.getProperty(CONFIG_REMOTE_NAME))
                .profile(environment.getProperty(CONFIG_REMOTE_PROFILE))
                .labels(RemoteConfigurationInitializerBase.splitLabelsOrEmpty(environment.getProperty(CONFIG_REMOTE_LABEL)))
                .retry(Boolean.TRUE.equals(environment.getProperty(CONFIG_REMOTE_RETRY, Boolean.class)))
                .readTimeout(environment.getProperty(CONFIG_REMOTE_READ_TIMEOUT, Integer.class, 1000))
                .connectTimeout(environment.getProperty(CONFIG_REMOTE_CONNECT_TIMEOUT, Integer.class, 1000))
                .build();
    }

    protected RemoteConfigObjectFactory createRemoteConfigObjectFactory() {
        return new OriginTrackedPojoFactory();
    }

    protected static String[] splitLabelsOrEmpty(String property) {
        return Optional.ofNullable(property)
                .map(s -> s.split(URL_SEPARATOR))
                .orElse(EMPTY_ARRAY);
    }

    private static PropertySource<?> delegateFetch(RetryContext context, Environment environment, RemoteConfigServiceClient client) {
        if(context.getRetryCount() > 0) {
            log.info("Retrying remote configuration. Attempt {}", context.getRetryCount());
        }
        return client.fetch(environment);
    }
}
