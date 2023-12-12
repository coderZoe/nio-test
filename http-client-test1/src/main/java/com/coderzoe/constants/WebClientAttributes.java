package com.coderzoe.constants;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yinhuasheng
 * @email yinhuasheng@unicloud.com
 * @date 2023/12/7 17:01
 */
public class WebClientAttributes {
    public static final String TIMEOUT = "UCA_NETWORK_WEB_CLIENT_TIME_OUT";

    public static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(10,
            200,
            20,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10),
            new ThreadPoolExecutor.AbortPolicy());
}
