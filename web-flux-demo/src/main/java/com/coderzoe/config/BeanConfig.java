package com.coderzoe.config;

import com.coderzoe.constants.WebClientAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.*;

import java.time.Duration;
/**
 * @author yinhuasheng
 * @email yinhuasheng@unicloud.com
 * @date 2023/12/6 9:26
 */
@Configuration
public class BeanConfig {
    public static final String DEFAULT_TIMEOUT_ATTRIBUTE = "defaultTimeout";

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
