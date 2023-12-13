package com.coderzoe.controller;

import com.coderzoe.constants.JsonUtil;
import com.coderzoe.constants.WebClientAttributes;
import com.coderzoe.model.Person;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.coderzoe.constants.WebClientAttributes.EXECUTOR_SERVICE;

/**
 * @author yinhuasheng
 * @email yinhuasheng@unicloud.com
 * @date 2023/12/6 9:26
 */
@RestController
@RequestMapping("/test1")

public class Test1Controller {
    private AsyncHttpClient asyncHttpClient;
    private WebClient webClient;
    public static final String TEST2_URL = "http://localhost:8082/test2";
    public static final String TEST2_FLUX_URL = "http://localhost:8082/test2/flux";
    public static final String TEST3_URL = "http://localhost:8083/test3";

    @GetMapping("/sync")
    public String test() throws ExecutionException, InterruptedException {
        Response response = asyncHttpClient.preparePost(TEST2_URL)
                .execute().get();
        if (response.getStatusCode() == 200) {
            return response.getResponseBody();
        }
        return null;
    }

    @GetMapping("/async")
    public void async() {
        asyncHttpClient.preparePost(TEST2_URL)
                .setReadTimeout(20000)
                .execute(new AsyncCompletionHandler<String>() {
                    @Override
                    public String onCompleted(Response response) throws Exception {
                        return response.getResponseBody();
                    }
                });
    }

    @GetMapping("/parallel")
    public void parallel() throws ExecutionException, InterruptedException {
        CompletableFuture<Response> completableFuture1 = asyncHttpClient.preparePost(TEST2_URL)
                .setReadTimeout(20000)
                .execute()
                .toCompletableFuture();
        CompletableFuture<Response> completableFuture2 = asyncHttpClient.preparePost(TEST3_URL)
                .setReadTimeout(20000)
                .execute()
                .toCompletableFuture();
        CompletableFuture.allOf(completableFuture1, completableFuture2).join();
        Response response1 = completableFuture1.get();
        Response response2 = completableFuture2.get();
        handle(response1.getResponseBody(), response2.getResponseBody());
    }

    @GetMapping("/flux")
    public Mono<String> flux() {
        return webClient.post().uri(TEST2_URL)
                .attribute(WebClientAttributes.TIMEOUT, Duration.ofSeconds(20))
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/flux2")
    public Mono<Person> flux2() {
        System.out.println(Thread.currentThread().toString());
        Person person = new Person("小明", 18, "翻斗花园");
        return webClient.post().uri(TEST2_FLUX_URL)
                .header("X-USER-ID", "aaabbbccc")
                .attribute(WebClientAttributes.TIMEOUT, Duration.ofSeconds(20))
                .bodyValue(person)
                .retrieve()
                .bodyToMono(Person.class);
    }

    @GetMapping("/flux/sync")
    public Person fluxSync() {
        Person person = new Person("小明", 18, "翻斗花园");
        return webClient.post().uri(TEST2_FLUX_URL)
                .header("X-USER-ID", "aaabbbccc")
                .attribute(WebClientAttributes.TIMEOUT, Duration.ofSeconds(20))
                .bodyValue(person)
                .retrieve()
                .bodyToMono(Person.class)
                .block();
    }

    @GetMapping("/flux/subscribe")
    public void fluxSubscribe() {
        webClient.post().uri(TEST2_URL)
                .attribute(WebClientAttributes.TIMEOUT, Duration.ofSeconds(20))
                .retrieve()
                .bodyToMono(String.class)
                .publishOn(Schedulers.fromExecutor(EXECUTOR_SERVICE))
                .subscribe(
                        str -> System.out.println("当前线程" + Thread.currentThread().getName() + "，收到的响应：" + str),
                        error -> System.out.println("当前线程" + Thread.currentThread().getName() + "，请求失败，失败原因：" + error.getMessage())
                );
    }

    @GetMapping("/flux/subscribe2")
    public void fluxSubscribe2() {
        webClient.post().uri(TEST2_URL)
//                .attribute(WebClientAttributes.TIMEOUT, Duration.ofSeconds(20))
                .retrieve()
                .bodyToMono(String.class)
                .publishOn(Schedulers.fromExecutor(EXECUTOR_SERVICE))
                .doOnSuccess(str -> System.out.println("当前线程" + Thread.currentThread().getName() + "，收到的响应：" + str))
                .doOnError(error -> System.out.println("当前线程" + Thread.currentThread().getName() + "，请求失败，失败原因：" + error.getMessage()))
                .subscribe();
    }

    @GetMapping("/flux/parallel")
    public void fluxParallel() {
        Mono<String> mono2 = webClient.post().uri(TEST2_URL)
                .attribute(WebClientAttributes.TIMEOUT, Duration.ofSeconds(20))
                .retrieve()
                .bodyToMono(String.class);
        Mono<String> mono3 = webClient.post().uri(TEST3_URL)
                .attribute(WebClientAttributes.TIMEOUT, Duration.ofSeconds(20))
                .retrieve()
                .bodyToMono(String.class);
        Mono.zip(mono2, mono3)
                .publishOn(Schedulers.fromExecutor(EXECUTOR_SERVICE))
                .subscribe(tuple -> {
                    String result2 = tuple.getT1();
                    String result3 = tuple.getT2();
                    handle(result2, result3);
                });
    }
    @GetMapping("/flux/delay")
    public void fluxDelay() {
        Person person = new Person("小明", 18, "翻斗花园");
        Mono.delay(Duration.ofMinutes(2))
                .then(webClient.post().uri(TEST2_FLUX_URL)
                        .header("X-USER-ID", "aaabbbccc")
                        .attribute(WebClientAttributes.TIMEOUT, Duration.ofSeconds(20))
                        .bodyValue(person)
                        .retrieve()
                        .bodyToMono(Person.class))
                .subscribe(
                        person1 -> System.out.println("收到的响应：" + JsonUtil.toJsonString(person1)),
                        error -> System.out.println("请求失败，失败原因：" + error.getMessage())
                );
    }

    @GetMapping("/flux/sequence")
    public void fluxSequence(){
        webClient.post().uri(TEST2_URL)
                .attribute(WebClientAttributes.TIMEOUT, Duration.ofSeconds(20))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(result -> {
                    Person person = new Person(result, 18, "翻斗花园");
                    return webClient.post().uri(TEST2_FLUX_URL)
                            .header("X-USER-ID", "aaabbbccc")
                            .attribute(WebClientAttributes.TIMEOUT, Duration.ofSeconds(20))
                            .bodyValue(person)
                            .retrieve()
                            .bodyToMono(Person.class);
                }).subscribe(person -> System.out.println("收到的响应：" + JsonUtil.toJsonString(person)),
                        error -> System.out.println("请求失败，失败原因：" + error.getMessage()));
    }

    private void handle(String response2, String response3) {
        System.out.println("response2:" + response2);
        System.out.println("response3:" + response3);
    }

    @Autowired
    public void setAsyncHttpClient(AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
    }

    @Autowired
    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }
}
