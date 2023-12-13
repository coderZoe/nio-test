package com.coderzoe.controller;

import com.coderzoe.model.Person;
import org.springframework.web.bind.annotation.*;

/**
 * @author yinhuasheng
 * @email yinhuasheng@unicloud.com
 * @date 2023/12/6 10:09
 */
@RestController
@RequestMapping("/test2")
public class Test2Controller {


    @PostMapping
    public String test2() throws InterruptedException {
        Thread.sleep(15000);
        return "test2";
    }

    @PostMapping("/flux")
    public Person test2Flux(@RequestBody  Person person, @RequestHeader("X-USER-ID")String userId) throws InterruptedException {
        Thread.sleep(15000);
        System.out.println(userId);
        return person;
    }
}
