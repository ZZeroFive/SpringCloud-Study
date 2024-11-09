package ml.spring.boot.breaker.controller;


import com.alibaba.fastjson2.JSON;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
@Slf4j
public class TestController {



    @CircuitBreaker(name = "WangJin", fallbackMethod = "fallback")
    @GetMapping("/{id}/info")
    public String test(@PathVariable("id") Integer id) {
        if (id < 10) {
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException e) {
                log.info("线程被中断");
            }
            return "latency " + id;
        } else if (id == 10) {
            throw new RuntimeException(id + "fallback");
        } else {
            return "normal " + id;
        }
    }

    public String fallback(Integer id, Throwable throwable) {
        log.info("异常信息： {}", JSON.toJSONString(throwable));
        return "fallback " + id;
    }
}
