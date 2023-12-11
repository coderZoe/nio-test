package com.coderzoe.controller;

import com.coderzoe.constants.WebClientAttributes;
import com.coderzoe.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author coderZoe
 * @date 2023/12/11 19:44
 */
@RestController
public class WebFluxController {
    private WebClient webClient;
    public static final String TEST2_FLUX_URL = "http://localhost:8082/test2/flux";

    @GetMapping("/flux")
    public Mono<Person> getPerson(){
        System.out.println(Thread.currentThread().toString());
        Person person = new Person("小明",18,"翻斗花园");
        return webClient.post().uri(TEST2_FLUX_URL)
                .header("X-USER-ID","aaabbbccc")
                .attribute(WebClientAttributes.TIMEOUT, Duration.ofSeconds(20))
                .bodyValue(person)
                .retrieve()
                .bodyToMono(Person.class);
    }

    @Autowired
    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }
}
