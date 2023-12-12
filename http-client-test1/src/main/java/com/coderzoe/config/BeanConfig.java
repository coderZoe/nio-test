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
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(3));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(timeoutException())
                .filter(logRequestFilter())
                .filter(logResponseFilter())
                .filter(exceptionFilter())
                .build();
    }

    private ExchangeFilterFunction timeoutException(){
        return (request, next) -> {
           return next.exchange(request)
                   .doOnError(error ->{
                log.error(request.url()+"请求超时");
            });
        };
    }

    private ExchangeFilterFunction timeoutFilter() {
        return (request, next) -> {
            System.out.println(request.getClass());
            Duration timeout= (Duration)(request.attribute(WebClientAttributes.TIMEOUT).orElse(null));
            if(timeout == null){
                return next.exchange(request).timeout(Duration.ofSeconds(3));
            }
            return next.exchange(request).timeout(timeout);
        };
    }

    private ExchangeFilterFunction logRequestFilter(){
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("webClient request,method:{},url:{},headers:{},body:{}",
                    clientRequest.method(),
                    clientRequest.url(),
                    JsonUtil.toJsonString(clientRequest.headers()),
                    JsonUtil.toJsonString(clientRequest.body()));
            return Mono.just(clientRequest);
        });
    }
    private ExchangeFilterFunction logResponseFilter(){
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("webClient response, status:{}",clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    private ExchangeFilterFunction exceptionFilter(){
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            // 检查响应是否表示错误（例如，4xx和5xx状态码）
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            // 处理错误响应体，可能抛出自定义异常
                            log.error("webclient request error:{}",errorBody);
                            return Mono.error(new RuntimeException("Error response received"));
                        });
            }
            return Mono.just(clientResponse);
        });
    }
}
