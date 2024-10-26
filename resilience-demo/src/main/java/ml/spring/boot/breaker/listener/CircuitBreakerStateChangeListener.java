package ml.spring.boot.breaker.listener;


import com.alibaba.fastjson2.JSON;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import io.github.resilience4j.core.EventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CircuitBreakerStateChangeListener implements EventConsumer<CircuitBreakerOnStateTransitionEvent> {


    @Override
    public void consumeEvent(CircuitBreakerOnStateTransitionEvent event) {
        CircuitBreaker.State oldState = event.getStateTransition().getFromState();
        CircuitBreaker.State newState = event.getStateTransition().getToState();
        log.info("服务端监听到断路器的状态发生变化! 旧状态: {} 现状态: {}" , JSON.toJSONString(oldState), JSON.toJSONString(newState));
    }
}
