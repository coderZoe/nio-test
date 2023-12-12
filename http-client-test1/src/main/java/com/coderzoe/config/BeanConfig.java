package com.coderzoe.config;

import com.coderzoe.constants.JsonUtil;
import com.coderzoe.constants.WebClientAttributes;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * @author yinhuasheng
 * @email yinhuasheng@unicloud.com
 * @date 2023/12/6 9:26
 */
@Configuration
@Slf4j
public class BeanConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Bean
    public AsyncHttpClient asyncHttpClient() {
        DefaultAsyncHttpClientConfig config = Dsl.config()
                .setConnectTimeout(3000)
                .setReadTimeout(3000)
                .build();
        return Dsl.asyncHttpClient(config);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .filter(timeoutFilter())
                .filter(exceptionFilter())
                .filter(logRequestFilter())
                .filter(logResponseFilter())
                .build();
    }

    private ExchangeFilterFunction exceptionFilter() {
        return (request, next) -> {
            return next.exchange(request)
                    .doOnError(error -> {
                        log.error(request.url() + "请求超时");
                    });
        };
    }

    private ExchangeFilterFunction timeoutFilter() {
        return (request, next) -> {
            System.out.println(request.getClass());
            Duration timeout = (Duration) (request.attribute(WebClientAttributes.TIMEOUT).orElse(null));
            if (timeout == null) {
                return next.exchange(request).timeout(Duration.ofSeconds(3));
            }
            return next.exchange(request).timeout(timeout);
        };
    }

    private ExchangeFilterFunction logRequestFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("webClient request,method:{},url:{},headers:{},body:{}",
                    clientRequest.method(),
                    clientRequest.url(),
                    JsonUtil.toJsonString(clientRequest.headers()),
                    JsonUtil.toJsonString(clientRequest.body()));
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponseFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("webClient response, status:{}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
