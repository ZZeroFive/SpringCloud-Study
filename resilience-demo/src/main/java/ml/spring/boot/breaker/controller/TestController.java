package ml.spring.boot.breaker.controller;


import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ml.spring.boot.breaker.service.RemoteService;
import ml.spring.boot.breaker.util.ResilienceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

@RequestMapping("/")
@RestController
@Slf4j
public class TestController {


    @Autowired
    private RemoteService remoteService;

    // @Autowired
    // private CircuitBreakerRegistry circuitBreakerRegistry;

    @GetMapping("/{id}")
    public String test(@PathVariable("id") int id) {
        // 调用远程服务
        System.out.println("id is " + id);
        // 不带断路器调用方式
        // return remoteService.remoteAPI(Integer.parseInt(id));

        remoteService.setId(id);
        // 带断路器调用方式
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("backendName");

        Retry retry = Retry.ofDefaults("backendName");
        Supplier<String> decorateSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, remoteService::remoteAPI);

        decorateSupplier = Retry.decorateSupplier(retry, decorateSupplier);
        String result = Try
                .ofSupplier(decorateSupplier)
                .recover(e -> "recover result")
                .get();
        return result;
    }

}
