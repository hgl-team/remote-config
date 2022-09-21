package org.hglteam.config.remote;

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface RemoteConfigServiceClient {
    public static final String SLASH_PLACEHOLDER = "(_)";

    PropertySource<?> fetch(Environment environment);

    static String normalizeLabel(String label) {
        if (label != null && label.contains("/")) {
            return label.replace("/", SLASH_PLACEHOLDER);
        }
        return label;
    }

    class RemoteConfigFetchException extends RuntimeException {
        public RemoteConfigFetchException(String message, Throwable cause) {
            super(message, cause);
        }

        public static Consumer<Throwable> causedBy(String message) {
            return e -> {
                throw new RemoteConfigFetchException(message, e);
            };
        }

        public static <T> Supplier<T> optionalErrorCausedBy(String message, Throwable cause) {
            return () -> {
                if(Objects.nonNull(cause)) {
                    throw new RemoteConfigFetchException(message, cause);
                } else return null;
            };
        }
    }
}
