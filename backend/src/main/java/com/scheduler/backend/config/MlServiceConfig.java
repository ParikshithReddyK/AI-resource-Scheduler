package com.scheduler.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@Configuration
public class MlServiceConfig {

    @Value("${ml.service.base-url}")
    private String mlServiceBaseUrl;

    @Bean
    public RestClient mlServiceRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        return RestClient.builder()
                .baseUrl(mlServiceBaseUrl)
                .requestFactory(factory)
                .requestInterceptor(loggingInterceptor())
                .build();
    }

    private ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            System.out.println("=== OUTGOING ML REQUEST ===");
            System.out.println("URI: " + request.getURI());
            System.out.println("Headers: " + request.getHeaders());
            System.out.println("Body: " + new String(body, StandardCharsets.UTF_8));
            System.out.println("===========================");
            ClientHttpResponse response = execution.execute(request, body);
            return response;
        };
    }
}