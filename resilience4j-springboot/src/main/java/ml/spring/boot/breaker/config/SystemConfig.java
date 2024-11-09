package ml.spring.boot.breaker.config;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SystemConfig {
    @Bean
    @Qualifier("restTemplate")
    public RestTemplate createRestTemplate() {
        return new RestTemplateBuilder()
                .rootUri("http://127.0.0.1:9090")
                .build();
    }
}
