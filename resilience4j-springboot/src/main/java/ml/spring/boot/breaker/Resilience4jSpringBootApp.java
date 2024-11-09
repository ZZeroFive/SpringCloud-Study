package ml.spring.boot.breaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class Resilience4jSpringBootApp {
    public static void main( String[] args ) {
        SpringApplication.run(Resilience4jSpringBootApp.class, args);
    }
}
