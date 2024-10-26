package ml.spring.boot.breaker.config;


import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import ml.spring.boot.breaker.listener.CircuitBreakerStateChangeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CircuitBreakerConfig {


    @Autowired
    private CircuitBreakerStateChangeListener listener;

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
                .automaticTransitionFromOpenToHalfOpenEnabled(true) // 自动从开启状态转换为半开启状态
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
}
