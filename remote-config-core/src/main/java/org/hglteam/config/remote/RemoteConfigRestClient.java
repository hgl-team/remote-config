package org.hglteam.config.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RemoteConfigRestClient implements RemoteConfigServiceClient {
    private static final Logger log = LoggerFactory.getLogger(RemoteConfigRestClient.class);
    private final RestTemplate restTemplate;
    private final RemoteConfigRestServiceProperties properties;
    private final RemoteConfigObjectFactory remoteConfigObjectFactory;

    public RemoteConfigRestClient(
            RestTemplate restTemplate,
            RemoteConfigRestServiceProperties properties,
            RemoteConfigObjectFactory originTrackedObjectFactory) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.remoteConfigObjectFactory = originTrackedObjectFactory;
    }

    @Override
    public PropertySource<?> fetch(Environment environment) {
        RemotePropertySource remotePropertySource = remoteConfigObjectFactory.createRemotePropertySource("remoteConfig");

        if(properties.getLabels().length == 0) {
            throw new RemoteConfigFetchException("No labels were provided.", new IllegalStateException());
        }

        for (String label : properties.getLabels()) {
            log.info("Fetching properties for label {}...", label);
            SerializableEnvironment remoteEnv = this.getRemoteEnvironment(label);

            if(Objects.nonNull(remoteEnv) && Objects.nonNull(remoteEnv.getPropertySources())) {
                Collection<PropertySource<?>> properties = remoteEnv.getPropertySources().stream()
                        .map(this::convertPropertySource)
                        .collect(Collectors.toList());

                properties.forEach(remotePropertySource::addPropertySource);

                log.info("Registered {} properties from label {}", properties.size(), label);

                if(StringUtils.hasText(remoteEnv.getState())
                        || StringUtils.hasText(remoteEnv.getState())) {
                    HashMap<String, Object> map = new HashMap<>();
                    putValue(map, "config.client.state", remoteEnv.getState());
                    putValue(map, "config.client.version", remoteEnv.getVersion());
                    remotePropertySource.addFirstPropertySource(new MapPropertySource("remoteConfig", map));
                }

                return remotePropertySource;
            } else {
                log.debug("No configuration found for label {}... Skip.", label);
            }
        }

        throw new RemoteConfigFetchException("Could not locate remote config.", new IllegalStateException());
    }

    private void putValue(HashMap<String, Object> map, String key, String value) {
        if(StringUtils.hasText(value)) {
            map.put(key, value);
        }
    }

    private PropertySource<?> convertPropertySource(SerializablePropertySource source) {
        Map<String, Object> properties = new HashMap<>();

        for (Map.Entry<?, ?> entry: source.getSource().entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if(value instanceof Map) {
                Map valueMap = (Map)value;
                value = remoteConfigObjectFactory.createPropertyValue(
                        source.getName(),
                        valueMap.get("origin"),
                        valueMap.get("value"));
            }

            properties.put(key, value);
        }

        return remoteConfigObjectFactory.createPropertySource(source.getName(), properties);
    }

    private SerializableEnvironment getRemoteEnvironment(String label) {
        String path = "/{name}/{profile}" + (StringUtils.hasText(label) ? "/{label}" : "");
        String normalizedLabel = RemoteConfigServiceClient.normalizeLabel(label);
        String[] args = Stream.of(properties.getName(), properties.getProfile(), normalizedLabel)
                .filter(StringUtils::hasText)
                .toArray(String[]::new);

        Throwable lastException = null;
        ResponseEntity<SerializableEnvironment> response = null;

        if(properties.getBaseUris().length == 0) {
            lastException = new RemoteConfigFetchException("No remote server URI's were provided.", new IllegalStateException());
        }

        for (String uri : properties.getBaseUris()) {
            try {
                final HttpEntity<Void> noEntity = new HttpEntity<>((Void)null);
                response = restTemplate.exchange(uri + path, HttpMethod.GET, noEntity, SerializableEnvironment.class, args);

                if(Optional.ofNullable(response)
                        .map(ResponseEntity::getStatusCode)
                        .map(HttpStatus.OK::equals)
                        .orElse(Boolean.FALSE)) {
                    break;
                }
            } catch (HttpClientErrorException e){
                log.debug(String.format("Http Error fetching configuration from '%s':", uri + path), e);
                if(e.getStatusCode() != HttpStatus.NOT_FOUND) {
                    lastException = e;
                }
            } catch (Exception e) {
                log.debug(String.format("Error fetching configuration from '%s':", uri + path), e);
                lastException = e;
            }
        }

        return Optional.ofNullable(response)
                .filter(r -> r.getStatusCode() == HttpStatus.OK)
                .map(ResponseEntity::getBody)
                .orElseGet(RemoteConfigFetchException.optionalErrorCausedBy(
                        String.format("Could not fetch config of label '%s'", label),
                        lastException));
    }
}
