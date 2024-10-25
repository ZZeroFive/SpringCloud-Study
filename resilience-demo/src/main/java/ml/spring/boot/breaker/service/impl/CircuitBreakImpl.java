package ml.spring.boot.breaker.service.impl;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import ml.spring.boot.breaker.service.LocalService;
import ml.spring.boot.breaker.service.RemoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Slf4j
@Service
public class CircuitBreakImpl implements LocalService {

    @Autowired
    private RemoteService remoteService;

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

    @Override
    public String callRemote(int id) {
        remoteService.setId(id);
        return onlyCircuitBreak();
    }
}
