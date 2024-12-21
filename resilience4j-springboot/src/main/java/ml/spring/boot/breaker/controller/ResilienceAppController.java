package ml.spring.boot.breaker.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import ml.spring.boot.breaker.service.ExternAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/")
public class ResilienceAppController {

    @Autowired
    private ExternAPIService externAPIService;


    @GetMapping("/{id}")
    // @CircuitBreaker(name = "circuitBeakerService", fallbackMethod = "fallback")
    @Retry(name = "retryApi", fallbackMethod = "fallbackRetry")
    public String circuitBeakerApi(@PathVariable(name = "id") Integer id) {
        return externAPIService.callApi(id);
    }

    public String fallback(Integer id, Throwable ex) {
        log.info("异常信息 " + ex);
        return "fallback " + id;
    }

    public String fallbackRetry(Integer id, Throwable ex) {
        log.info("Retry异常信息 " + ex);
        return "retry fallback " + id;
    }
}
