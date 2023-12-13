package com.coderzoe.config;

import com.coderzoe.constants.JsonUtil;
import com.coderzoe.constants.WebClientAttributes;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

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
                .filter(logFilter())
                .build();
    }

    private ExchangeFilterFunction timeoutFilter() {
        return (request, next) -> {
            Duration timeout = (Duration) (request.attribute(WebClientAttributes.TIMEOUT).orElse(Duration.ofSeconds(3)));
            String logId = (String)(request.attribute("org.springframework.web.reactive.function.client.ClientRequest.LOG_ID").orElse(null));
            return next.exchange(request).timeout(timeout).doOnError(error ->{
                if(error instanceof TimeoutException){
                    log.error("请求超时，超时时间：{}，请求信息：[logId:{},url:{},method:{},header:{}]",
                            timeout,logId,request.url(),request.method(),request.headers());
                }else {
                    log.error("请求异常，异常原因：{}，请求信息：[logId:{},url:{},method:{},header:{}]",
                            error.getMessage(),logId,request.url(),request.method(),request.headers());
                }
            });
        };
    }
    private ExchangeFilterFunction logFilter() {
        return (request, next) -> {
            String logId = (String)(request.attribute("org.springframework.web.reactive.function.client.ClientRequest.LOG_ID").orElse(null));
            log.info("收到请求，logId:{},url:{},method:{},headers:{},requestBody:{}",logId,request.url(),request.method(),request.headers(),JsonUtil.toJsonString(request.body()));
            return next.exchange(request).publishOn(Schedulers.boundedElastic()).flatMap(clientResponse -> {
                if(clientResponse.statusCode().isError()){
                    log.error("收到响应，但响应异常，响应状态码{}，响应相对的请求信息：[logId:{},url:{},method:{},header:{}]",
                            clientResponse.statusCode(),
                            logId,request.url(),
                            request.method(),
                            request.headers());
                }else {
                    log.info("收到响应，响应正常，响应状态码{}，响应相对的请求信息：[logId:{},url:{},method:{},header:{}]",
                            clientResponse.statusCode(),
                            logId,request.url(),
                            request.method(),
                            request.headers());
                }
               return Mono.just(clientResponse);
           });
        };
    }
}
