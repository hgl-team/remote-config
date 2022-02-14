package org.hglteam.config.remote;

import com.google.gson.Gson;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

public class RestRemoteConfigurationInitializer extends RemoteConfigurationInitializerBase<
        RemoteConfigRestClient,
        RemoteConfigRestServiceProperties,
        RemoteConfigRestServiceProperties.RemoteConfigRestServicePropertiesBuilder<?, ?>> {
    private static final String CONFIG_REMOTE_URL = "config.remote.url";

    @Override
    protected RemoteConfigRestServiceProperties.RemoteConfigRestServicePropertiesBuilder<?, ?> getRemoteConfigClientPropertyBuilder(Environment environment) {
        return RemoteConfigRestServiceProperties.builder()
                .baseUris(RemoteConfigurationInitializerBase.splitLabelsOrEmpty(environment.getProperty(CONFIG_REMOTE_URL)));
    }

    @Override
    protected RemoteConfigRestClient createRemoteConfigServiceClient(RemoteConfigRestServiceProperties properties, RemoteConfigObjectFactory objectFactory) {
        RestTemplate restTemplate = createRestTemplate(properties);
        return new RemoteConfigRestClient(restTemplate, properties, objectFactory);
    }

    private RestTemplate createRestTemplate(RemoteConfigRestServiceProperties properties) {
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
}
