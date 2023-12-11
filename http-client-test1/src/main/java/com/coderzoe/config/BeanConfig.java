package com.coderzoe.config;

import com.coderzoe.constants.WebClientAttributes;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author yinhuasheng
 * @email yinhuasheng@unicloud.com
 * @date 2023/12/6 9:26
 */
@Configuration
public class BeanConfig {
    public static final String DEFAULT_TIMEOUT_ATTRIBUTE = "defaultTimeout";

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
                .build();
    }

    private ExchangeFilterFunction timeoutFilter() {
        return (request, next) -> {
            Duration timeout= (Duration)(request.attribute(WebClientAttributes.TIMEOUT).orElse(null));
            if(timeout == null){
                return next.exchange(request).timeout(Duration.ofSeconds(3));
            }
            return next.exchange(request).timeout(timeout);
        };
    }
}
