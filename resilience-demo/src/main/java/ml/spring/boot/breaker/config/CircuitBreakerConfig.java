package ml.spring.boot.breaker.config;


import com.alibaba.fastjson2.JSON;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import ml.spring.boot.breaker.listener.CircuitBreakerStateChangeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class CircuitBreakerConfig {


    @Autowired
    private CircuitBreakerStateChangeListener listener;


    /**
     * 异常降级+延迟降级+断路器+状态自动变为HALF_OPEN
     * @return
     */
    @Bean
    @Qualifier("countBasedBreaker")
    public CircuitBreaker countBasedBreaker() {
        // 抽样10次调用 至少10次调用 失败率达到50% --> 开启断路器
        // 等待10秒后切换成半开
        // 切换成半开后允许放入20个请求，成功率需要是50%
        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig config = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(100) // 失败率
                .minimumNumberOfCalls(5) // 最低调用次数
                .slidingWindowType(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // 抽样数据窗口类型:基于个数
                .slidingWindowSize(10) // 抽样窗口大小
                .waitDurationInOpenState(Duration.ofSeconds(10)) // open 状态等待10秒才会转换成half open 默认不自动转换，只是一个门槛
                .automaticTransitionFromOpenToHalfOpenEnabled(false) // 自动从开启状态转换为半开启状态
                .permittedNumberOfCallsInHalfOpenState(20) // 半开状态下允许的调用个数
                .slowCallRateThreshold(50) // 慢调用降级阈值
                .slowCallDurationThreshold(Duration.ofSeconds(3)) // 慢调用持续时间
                .build();

        CircuitBreaker circuitBreaker = CircuitBreaker.of("countBasedBreaker", config);

        // 向断路器注册事件监听器，这样断路器状态改变时，我们可以第一时间收到消息
        circuitBreaker.getEventPublisher()
                        .onStateTransition(listener);
        // 创建断路器
        return circuitBreaker;
    }

    /**
     * 重试Retry机制
     * @return
     */
    @Bean
    @Qualifier("retryCircuitBreaker")
    public Retry retryBasedBreaker() {
        RetryConfig retryConfig = RetryConfig.custom()
                .retryOnResult( r -> true) // 什么样的返回结果进行retry 都执行
                .maxAttempts(3) // 重试次数
                .waitDuration(Duration.ofMillis(500)) // 重试间隔
                .failAfterMaxAttempts(true) // 进行最大重试次数后 依然失败怎么办 要不要返回失败
                .build();
        Retry retry = Retry.of("threeTimesRetry", retryConfig);
        retry.getEventPublisher()
                .onRetry(event -> {
                    log.info("触发retry机制... {}", JSON.toJSONString(event));
                }); // 重试时执行的逻辑

        return retry;
    }

    /**
     * 限制调用执行的时间
     * @return
     */
    @Bean
    @Qualifier("timeLimiter")
    public TimeLimiter timeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(1)) // 1秒调用
                .cancelRunningFuture(false) // 取消异步计算
                .build();
        TimeLimiter limiter = TimeLimiter.of("timeLimiter", config);

        limiter.getEventPublisher()
                .onError(e -> {log.info("限时器：执行失败! {}", JSON.toJSONString(e));})
                .onSuccess(e -> log.info("限时器： 远程调用执行成功!"))
                .onTimeout(e -> log.info("限时器： 远程调用超出限定时间!"));
        return limiter;
    }

    /**
     * 限流： 限制一段时间内允许多少请求通过，多余的请求要么延迟执行，要么拒绝执行
     * 速率限制: 保护系统，防止过载引发问题
     * @return
     */
    @Bean
    @Qualifier("rateLimiter")
    public RateLimiter rateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(3)) // 限流的时间周期 3分钟内允许10个
                .timeoutDuration(Duration.ofMillis(100)) // 达到限流的速率后，调用者需要等待时间是多久
                .limitForPeriod(10) // 周期内最大的允许数
                .build();

        RateLimiter rateLimiter = RateLimiter.of("rateLimiter", config);
        rateLimiter.getEventPublisher()
                .onSuccess(event -> {log.info("【速率限制RateLimiter】监听到速率限制: 被成功调用！");})
                .onFailure(event -> log.info("【速率限制RateLimiter】监听到速率限制： 调用失败！"));

        return rateLimiter;
    }

    /**
     * 限流： 限制某一方法同时可以被多少线程调用
     * 瞬时并发限制：保护系统，防止过载引发问题
     * @return
     */
    @Bean
    @Qualifier("bulkhead")
    public Bulkhead bulkhead() {
        // 这是spring boot提供较为严格的限流 瞬时允许25个请求
        // spring boot提供的另外一个较为宽松的限流 20ms允许50个请求

        // 这里设置0秒5个请求
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(1)
                .maxWaitDuration(Duration.ZERO)
                .fairCallHandlingStrategyEnabled(false)
                .build();

        Bulkhead bulkhead = Bulkhead.of("ristrictBulkhead", config);

        bulkhead
                .getEventPublisher()
                .onCallPermitted(event -> log.info("【舱壁限流】允许访问 {}", JSON.toJSONString(event)))
                .onCallRejected(event -> log.info("【舱壁限流】 拒绝访问 {}", JSON.toJSONString(event)));
        return bulkhead;
    }

}
