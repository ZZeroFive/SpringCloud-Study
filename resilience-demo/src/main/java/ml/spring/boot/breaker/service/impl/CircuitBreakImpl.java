package ml.spring.boot.breaker.service.impl;

import com.alibaba.fastjson2.JSON;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import ml.spring.boot.breaker.service.LocalService;
import ml.spring.boot.breaker.service.RemoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
@Service
public class CircuitBreakImpl implements LocalService {

    @Autowired
    private RemoteService remoteService;

    @Autowired
    private CircuitBreaker circuitBreaker;

    @Autowired
    private Retry retry;

    @Autowired
    private TimeLimiter limiter;

    /**
     * 只通过断路器 装饰调用
     * @return
     */
    public String onlyCircuitBreak() {
        log.info("只通过断路器装饰...");
        // 断路器
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("backendName");
        //  断路器装饰
        Supplier<String> decorateSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, remoteService::remoteAPI);
        // 返回结果
        return Try
                .ofSupplier(decorateSupplier)
                .recover(e -> "recover result")
                .get();
    }

    /**
     * 自定义断路器配置进行远程服务调用
     * 断路器的工作原理在学习笔记里面，这里直接配置，并说明是什么参数
     * @return
     */
    public String diyCircuitBreaker() {
        log.info("通过自定义断路器装饰...");
        //  断路器装饰
        Supplier<String> decorateSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, remoteService::remoteAPI);
        // 返回结果
        return Try
                .ofSupplier(decorateSupplier)
                .recover(this::fallback)
                .get();
    }

    /**
     * 只对接口进行retry
     * 只会不停得retry 对于retry失败会返回降级结果，但是不会有断路器的逻辑
     * @return
     */
    public String reTryOnly() {
        log.info("通过自定义retry装饰...");
        Supplier<String> retrySupplier = Retry.decorateSupplier(retry, remoteService::remoteAPI);
        return Try.ofSupplier(retrySupplier)
                .recover(throwable -> {
                    log.info("retry三次后执行降级逻辑...");
                    return "重试失败降级";
                })
                .get();
    }

    /**
     * 重试机制+断路器融合
     * 重试发出的调用也算做一次统计
     * @return
     */
    public String retryWithCircuitBreaker() {
        log.info("通过断路器+retry装饰...");
        // 先用断路器装饰
        Supplier<String> decorateSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, remoteService::remoteAPI);
        decorateSupplier = Retry.decorateSupplier(retry, decorateSupplier);
        return Try.ofSupplier(decorateSupplier)
                .recover(throwable -> {
                    log.info("retry三次后执行降级逻辑...");
                    return "重试失败降级";
                })
                .get();
    }

    private String onlyLimitTime() throws ExecutionException, InterruptedException {
        log.info("通过限时器装饰...");
        // 创建定时线程池 大小为3
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
        // limiter要求supplier返回future子类
        // 所以借助JDK CompletableFuture执行调用远程服务，会返回一个Future对象
        // 这个是异步运算
        CompletableFuture<String> supplier = limiter.executeCompletionStage(scheduler, ()-> CompletableFuture.supplyAsync(remoteService::remoteAPI)).toCompletableFuture();

        return supplier.get();
    }

    private String fallback(Throwable throwable) {
        // 错误降级 和 延迟降级都会执行该逻辑
        log.error("执行断路器降级逻辑: {}", JSON.toJSONString(throwable));
        return "降级结果";
    }


    @Override
    public String callRemote(int id) throws Exception {
        remoteService.setId(id);
        // return onlyCircuitBreak();
        // return diyCircuitBreaker();
        // return reTryOnly();
        // return retryWithCircuitBreaker();
        return onlyLimitTime();
    }


}
