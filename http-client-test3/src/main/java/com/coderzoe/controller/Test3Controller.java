package com.coderzoe.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author yinhuasheng
 * @email yinhuasheng@unicloud.com
 * @date 2023/12/12 15:48
 */
@RestController
@RequestMapping("/test3")
public class Test3Controller {

    @PostMapping
    public String test() throws InterruptedException {
        TimeUnit.SECONDS.sleep(15);
        return "test3";
    }
}
