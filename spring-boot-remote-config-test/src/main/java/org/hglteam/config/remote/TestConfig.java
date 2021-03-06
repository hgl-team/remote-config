package org.hglteam.config.remote;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRetry
public class TestConfig implements InitializingBean {
    @Value("${application.datasource.username}")
    private String name;


    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("AAAAAAAA " + name);
    }

    @Bean("remoteConfigRetryTemplate")
    public static RetryTemplate remoteConfigRetryInterceptor() {
        return RetryTemplate.builder()
                .uniformRandomBackoff(1000, 10000)
                .maxAttempts(10)
                .build();
    }
}
